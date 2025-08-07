# Fintrip Database Schema Analysis

## Overview
This is a comprehensive expense management and corporate travel system with multi-tenant architecture. The database handles organizational hierarchy, expense tracking, approval workflows, and financial settlements.

## Core Entity Groups

### 1. Organizational Structure
```
Country → Cities → Office → Department → Grade → Employee
```

**Key Relationships:**
- **Office** belongs to a **City** and an **Entity**
- **Department** belongs to an **Office**
- **Grade** belongs to a **Department**
- **Employee** belongs to an **Office**, **Department**, and **Grade**
- **Employee** can have a **Manager** (self-referencing relationship)

### 2. Service Management
```
Category → Services → ProgrammeMappings
```

**Key Relationships:**
- **Services** belong to **Categories**
- **ProgrammeMappings** link **Services** to **Programmes** (many-to-many)
- **ProgrammePolicy** defines policies for programmes

### 3. Transaction Processing
```
Transactions ← TransactionOwners
Transactions ← TransactionGraywolf
Transactions → ExpenseVouchers
```

**Key Relationships:**
- **Transactions** are categorized by **SuperCategory**
- **Transactions** belong to **Office** and **Department**
- **TransactionOwners** track who benefits from transactions
- **TransactionGraywolf** stores additional processing metadata

### 4. Financial Settlement
```
ExpenseVouchers → EmployeeSettlements → SettlementStatements
```

**Key Relationships:**
- **ExpenseVouchers** group transactions for approval
- **EmployeeSettlements** handle financial settlements
- **SettlementStatements** provide detailed financial records

### 5. Permission System
```
Permissions ← PermissionGranters
```

**Key Relationships:**
- **Permissions** are granted to **Employees**
- **PermissionGranters** define who can approve permissions
- Multi-level approval workflow with ranking

## Detailed Relationship Analysis

### Foreign Key Relationships

| Entity | Foreign Key | References | Relationship Type |
|--------|-------------|------------|-------------------|
| Office | city_id | Cities | Many-to-One |
| Office | entity_id | Entities | Many-to-One |
| Department | office_id | Office | Many-to-One |
| Grade | department_id | Department | Many-to-One |
| Employee | level_id | Grade | Many-to-One |
| Employee | department_id | Department | Many-to-One |
| Employee | office_id | Office | Many-to-One |
| Employee | manager | Employee | Self-referencing |
| Services | category_id | Category | Many-to-One |
| ProgrammeMappings | service_id | Services | Many-to-One |
| Transactions | super_category_id | SuperCategory | Many-to-One |
| Transactions | office_id | Office | Many-to-One |
| Transactions | department_id | Department | Many-to-One |
| TransactionOwners | transactions_id | Transactions | Many-to-One |
| ExpenseVouchers | employee_id | Employee | Many-to-One |
| ExpenseVouchers | office_id | Office | Many-to-One |
| EmployeeSettlements | owner_id | Employee | Many-to-One |
| EmployeeSettlements | office_id | Office | Many-to-One |
| SettlementStatements | settlement_id | EmployeeSettlements | Many-to-One |
| Permissions | employee_id | Employee | Many-to-One |
| PermissionGranters | permission_id | Permissions | Many-to-One |
| TransactionGraywolf | transaction_id | Transactions | Many-to-One |
| TransactionGraywolf | employee_id | Employee | Many-to-One |

### Inheritance Hierarchy

```
SimpleModel (base)
├── Cities
├── Country
└── Entities

MultiTenantBaseModel (base)
├── ProgrammePolicy
├── Transactions
├── TransactionOwners
├── Permissions
├── PermissionGranters
└── TransactionGraywolf

MultiTenantTagsAttrsBaseModel (base)
├── Office
└── Department

MultiTenantTagsBaseModel (base)
└── Grade

MultiTenantLedgerModel (base)
└── Employee

MultiTenantAttrsBaseModel (base)
├── ExpenseVouchers
├── ExpenseVoucherType
└── EmployeeSettlements

MultiTenant (interface)
├── Category
├── Services
└── ProgrammeMappings
```

## Design Patterns Identified

### 1. Multi-Tenancy Pattern
- All entities include `company_id` for data isolation
- Base classes implement multi-tenant functionality
- Allows multiple companies to use the same database

### 2. Audit Trail Pattern
- `created_at` and `updated_at` timestamps on all entities
- Soft delete capability (`deleted` boolean flag)
- Comprehensive logging and tracking

### 3. JSONB Flexible Schema Pattern
- Uses PostgreSQL JSONB for flexible attributes
- Examples: `coords`, `profile`, `attrs`, `tags`, `content`
- Allows schema evolution without migrations

### 4. Hierarchical Organization Pattern
- Clear organizational hierarchy: Country → City → Office → Department → Grade → Employee
- Self-referencing relationships for management structure
- Supports complex organizational structures

### 5. Workflow Pattern
- Multi-level approval workflows (Permissions → PermissionGranters)
- Status tracking throughout the process
- Audit trail for all workflow steps

### 6. Junction Table Pattern
- `ProgrammeMappings` serves as junction table between Services and Programmes
- Enables many-to-many relationships with additional attributes

### 7. Polymorphic Association Pattern
- `owner_type` and `owner_id` fields for flexible ownership
- `MetaType` enum defines possible owner types
- JSONB `owner` field stores owner details

## Data Types and Constraints

### Primary Keys
- All entities use `bigint` primary keys
- Auto-generated sequential IDs

### Foreign Keys
- Consistent use of `bigint` for foreign keys
- Proper indexing on foreign key columns

### Enums
- `EmployeeStatus`: ACTIVE, INACTIVE, etc.
- `EmployeeType`: PERMANENT, CONTRACT, etc.
- `TransactionType`: SELFPAID, REIMBURSABLE, etc.
- `BookingStatus`: ONGOING, COMPLETED, etc.
- `PermissionStatus`: PENDING, APPROVED, REJECTED, etc.

### Special Data Types
- `UUID` for security-sensitive fields (claim, token_secret)
- `JSONB` for flexible schema fields
- `TEXT` for long content (ticket field)
- `TIMESTAMP` for date/time tracking

## Indexing Strategy

### Explicit Indexes
- `@Index` on `Cities.name`
- `@Index` on `Employee.code`
- Unique constraint on `Employee(code, company_id)`

### Implicit Indexes
- Primary key indexes
- Foreign key indexes (automatically created)
- Unique constraint indexes

## Security Considerations

### Password Security
- `@JsonIgnore` on password field prevents serialization
- Password field is required and non-null

### Token Security
- UUID-based tokens for claims and secrets
- Token-based permission granting system

### Data Isolation
- Multi-tenant architecture with company_id isolation
- Proper foreign key constraints maintain referential integrity

## Performance Considerations

### JSONB Usage
- Efficient storage and querying of flexible data
- Indexable JSONB fields for performance
- Reduces need for multiple related tables

### Lazy Loading
- `@ManyToOne(fetch = FetchType.LAZY)` on Entity relationship
- Prevents N+1 query problems

### Soft Deletes
- Maintains referential integrity
- Allows data recovery if needed
- Reduces cascade delete complexity

## Business Logic Insights

### Expense Management Flow
1. Employee creates transactions
2. Transactions are grouped into vouchers
3. Vouchers go through approval workflow
4. Approved vouchers create settlements
5. Settlements generate financial statements

### Permission Workflow
1. Permission request created for employee
2. Multiple granters can approve/reject
3. Rank-based approval system
4. Audit trail of all decisions

### Multi-level Organization
- Supports complex organizational hierarchies
- Flexible management structures
- Geographic and functional organization

This database design supports a comprehensive corporate expense management system with robust multi-tenancy, audit trails, and flexible workflow capabilities. 