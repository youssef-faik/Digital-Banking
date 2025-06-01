# Digital Banking Application Architecture

```mermaid
graph TB
    %% Client Layer
    subgraph "Frontend (Angular)"
        UI["User Interface"]
        
        subgraph "Angular Components"
            AC["Auth Components"]
            CC["Customer Components"]
            BC["Bank Account Components"]
            OC["Operation Components"]
            DC["Dashboard Components"]
            PC["Profile Components"]
            SC["Shared Components"]
        end
        
        subgraph "Angular Services"
            AS["Auth Service"]
            CS["Customer Service"]
            BS["Bank Account Service"]
            OS["Operation Service"]
            DS["Dashboard Service"]
            INTS["HTTP Interceptors"]
        end
        
        UI --> AC & CC & BC & OC & DC & PC
        AC & CC & BC & OC & DC & PC --> AS & CS & BS & OS & DS
        AS & CS & BS & OS & DS --> INTS
    end
    
    %% API Gateway
    INTS -->|HTTP/REST| APIGateway["API Gateway"]
    
    %% Backend Layer
    subgraph "Backend (Spring Boot)"
        subgraph "Presentation Layer (Controllers)"
            AuthC["AuthController"]
            CustC["CustomerController"]
            BankC["BankAccountController"]
            DashC["DashboardController"]
            SwagC["SwaggerRedirectController"]
        end
        
        subgraph "Business Layer (Services)"
            AuthS["AuthService"]
            CustS["CustomerService"]
            BankS["BankAccountService"]
            UserS["UserDetailsService"]
        end
        
        subgraph "Data Access Layer (Repositories)"
            UserR["UserRepository"]
            CustR["CustomerRepository"]
            BankR["BankAccountRepository"]
            OperR["AccountOperationRepository"]
        end
        
        subgraph "Domain Layer (Entities)"
            User["AppUser/Role"]
            Cust["Customer"]
            Bank["BankAccount (Current/Saving)"]
            Oper["AccountOperation"]
        end
        
        subgraph "Security Layer"
            JWT["JWT Authentication"]
            Sec["Security Config"]
            Auth["Authorization"]
        end
        
        subgraph "Configuration"
            OpenAPI["OpenAPI/Swagger Config"]
            PropSource["Property Source Config"]
            WebMVC["Web MVC Config"]
        end
        
        %% Connect the layers
        APIGateway --> AuthC & CustC & BankC & DashC & SwagC
        
        AuthC --> AuthS
        CustC --> CustS
        BankC --> BankS
        DashC --> BankS & CustS
        
        AuthS --> UserR & JWT
        CustS --> CustR
        BankS --> BankR & OperR
        UserS --> UserR
        
        UserR --> User
        CustR --> Cust
        BankR --> Bank
        OperR --> Oper
        
        JWT --> Auth
        Auth --> Sec
    end
    
    %% Database Layer
    subgraph "Persistence Layer"
        DB[(MySQL Database)]
    end
    
    %% Connect to DB
    UserR & CustR & BankR & OperR --> DB
    
    %% External Documentation
    subgraph "API Documentation"
        SwagUI["Swagger UI"]
    end
    
    SwagC --> SwagUI
    OpenAPI --> SwagUI
```

## Architecture Description

### Frontend (Angular)
- **User Interface**: The presentation layer that users interact with
- **Angular Components**: Modular UI components for different features
- **Angular Services**: Handle data operations and business logic on the client side

### Backend (Spring Boot)
- **Presentation Layer**: REST API controllers that handle HTTP requests
- **Business Layer**: Services implementing business logic and rules
- **Data Access Layer**: Repositories that interact with the database
- **Domain Layer**: Entity classes representing business objects
- **Security Layer**: JWT authentication and authorization mechanisms
- **Configuration**: Application and API documentation configuration

### Persistence Layer
- **MySQL Database**: Stores all application data

### API Documentation
- **Swagger UI**: Interactive API documentation and testing interface

## Data Flow
1. Users interact with the Angular frontend
2. Angular services make HTTP requests to the backend API
3. API controllers process requests and delegate to services
4. Services apply business logic and use repositories
5. Repositories perform CRUD operations on the database
6. Response flows back through the layers to the user interface

This architecture follows a typical multi-tier application design with separation of concerns and clear boundaries between layers.
