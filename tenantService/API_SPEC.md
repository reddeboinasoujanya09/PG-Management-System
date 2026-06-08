# Tenant Service — API Specification

**Base URL:** `http://localhost:8081`  
**API Prefix:** `/api/v1/tenants`  
**Full URL:** `http://localhost:8081/api/v1/tenants`

---

## Data Models

### Tenant (Response)

```json
{
  "tenantId":          "tenant-<uuid>",
  "tenantName":        "string",
  "tenantEmail":       "string (unique)",
  "tenantPhoneNumber": "string",
  "tenantAddress":     "string",
  "tenantStatus":      "TO_BE_RESERVED | RESERVED | PRESENT | TO_BE_VACATED | VACATED",
  "tenantType":        "PERMANENT | TEMPORARY",
  "createdAt":         "2026-06-08T10:00:00.000+00:00",
  "updatedAt":         "2026-06-08T10:00:00.000+00:00 | null",
  "vacateDate":        "2026-12-31T00:00:00.000+00:00 | null"
}
```

### TenantStatus values

| Value           | Meaning                              |
|-----------------|--------------------------------------|
| `TO_BE_RESERVED`| Registered, room not yet allocated   |
| `RESERVED`      | Room allocated, not moved in yet     |
| `PRESENT`       | Currently living in the PG           |
| `TO_BE_VACATED` | Serving notice period                |
| `VACATED`       | Has moved out                        |

### TenantType values

| Value       | Meaning                              |
|-------------|--------------------------------------|
| `PERMANENT` | No fixed end date; `vacateDate` = null |
| `TEMPORARY` | Fixed stay; `vacateDate` required    |

### Error Response (all errors)

```json
{
  "timestamp": "2026-06-08T10:00:00.000Z",
  "status":    409,
  "error":     "Conflict",
  "message":   "Human readable reason"
}
```

### Validation Error Response (400)

```json
{
  "timestamp": "2026-06-08T10:00:00.000Z",
  "status":    400,
  "error":     "Validation Failed",
  "fieldErrors": {
    "tenantName":  "Tenant name is required",
    "tenantEmail": "Invalid email format"
  }
}
```
## Endpoints Table

| Method   | Path                          | Description              |
|----------|-------------------------------|--------------------------|
| `POST`   | `/api/v1/tenants`             | Create a new tenant      |
| `GET`    | `/api/v1/tenants/{tenantId}`  | Get tenant by ID         |
| `GET`    | `/api/v1/tenants/search?name=`| Search tenants by name   |
| `PUT`    | `/api/v1/tenants/{tenantId}`  | Update tenant            |
| `DELETE` | `/api/v1/tenants/{tenantId}`  | Delete tenant            |

---

## Endpoints

---

### 1. Create Tenant

```
POST /api/v1/tenants
```

Registers a new tenant. Status is auto-set to `TO_BE_RESERVED`.

**Request Body**

```json
{
  "tenantName":        "Soujanya",
  "tenantEmail":       "soujanya@example.com",
  "tenantPhoneNumber": "9034763764",
  "tenantAddress":     "Hyderabad, India",
  "tenantType":        "TEMPORARY",
  "vacateDate":        "2026-12-31T00:00:00.000Z"
}
```

| Field             | Type      | Required | Validation                                      |
|-------------------|-----------|----------|-------------------------------------------------|
| `tenantName`      | string    | Yes      | Must not be blank                               |
| `tenantEmail`     | string    | Yes      | Valid email format; must be unique              |
| `tenantPhoneNumber`| string   | Yes      | Must not be blank                               |
| `tenantAddress`   | string    | No       | -                                               |
| `tenantType`      | enum      | Yes      | `PERMANENT` or `TEMPORARY`                      |
| `vacateDate`      | timestamp | Conditional | Required and must be future date if `tenantType` = `TEMPORARY`; ignored if `PERMANENT` |

**Responses**

| Status | Meaning              | Body          |
|--------|----------------------|---------------|
| `201`  | Tenant created       | Tenant object |
| `400`  | Validation failed    | Error object  |
| `409`  | Email already exists | Error object  |

**201 Example**

```
Location: /api/v1/tenants/tenant-550e8400-e29b-41d4-a716-446655440000
```
```json
{
  "tenantId":          "tenant-550e8400-e29b-41d4-a716-446655440000",
  "tenantName":        "Soujanya",
  "tenantEmail":       "soujanya@example.com",
  "tenantPhoneNumber": "9034763764",
  "tenantAddress":     "Hyderabad, India",
  "tenantStatus":      "TO_BE_RESERVED",
  "tenantType":        "TEMPORARY",
  "createdAt":         "2026-06-08T10:00:00.000+00:00",
  "updatedAt":         null,
  "vacateDate":        "2026-12-31T00:00:00.000+00:00"
}
```

---

### 2. Get Tenant by ID

```
GET /api/v1/tenants/{tenantId}
```

**Path Parameters**

| Parameter  | Type   | Required | Example                                        |
|------------|--------|----------|------------------------------------------------|
| `tenantId` | string | Yes      | `tenant-550e8400-e29b-41d4-a716-446655440000`  |

**Responses**

| Status | Meaning        | Body          |
|--------|----------------|---------------|
| `200`  | Found          | Tenant object |
| `404`  | Not found      | Error object  |

**200 Example**

```json
{
  "tenantId":          "tenant-550e8400-e29b-41d4-a716-446655440000",
  "tenantName":        "Soujanya",
  "tenantEmail":       "soujanya@example.com",
  "tenantPhoneNumber": "9034763764",
  "tenantAddress":     "Hyderabad, India",
  "tenantStatus":      "PRESENT",
  "tenantType":        "PERMANENT",
  "createdAt":         "2026-06-08T10:00:00.000+00:00",
  "updatedAt":         "2026-06-09T08:00:00.000+00:00",
  "vacateDate":        null
}
```

**404 Example**

```json
{
  "timestamp": "2026-06-08T10:00:00.000Z",
  "status":    404,
  "error":     "Tenant Not Found",
  "message":   "Tenant not found with id: tenant-xxx"
}
```

---

### 3. Search Tenants by Name

```
GET /api/v1/tenants/search?name={name}
```

Case-insensitive partial match on `tenantName`. Returns empty array if no matches or blank query.

**Query Parameters**

| Parameter | Type   | Required | Example  |
|-----------|--------|----------|----------|
| `name`    | string | Yes      | `souj`   |

**Responses**

| Status | Meaning                     | Body                |
|--------|-----------------------------|---------------------|
| `200`  | Results (may be empty list) | Array of Tenant     |

**200 Example**

```json
[
  {
    "tenantId":          "tenant-550e8400-e29b-41d4-a716-446655440000",
    "tenantName":        "Soujanya",
    "tenantEmail":       "soujanya@example.com",
    "tenantPhoneNumber": "9034763764",
    "tenantAddress":     "Hyderabad, India",
    "tenantStatus":      "PRESENT",
    "tenantType":        "PERMANENT",
    "createdAt":         "2026-06-08T10:00:00.000+00:00",
    "updatedAt":         null,
    "vacateDate":        null
  }
]
```

---

### 4. Update Tenant

```
PUT /api/v1/tenants/{tenantId}
```

Full update of a tenant. All required fields must be sent.  
`tenantStatus` can be used to move a tenant through the lifecycle.

**Path Parameters**

| Parameter  | Type   | Required | Example                                       |
|------------|--------|----------|-----------------------------------------------|
| `tenantId` | string | Yes      | `tenant-550e8400-e29b-41d4-a716-446655440000` |

**Request Body**

```json
{
  "tenantName":        "Soujanya R",
  "tenantEmail":       "soujanya@example.com",
  "tenantPhoneNumber": "9034763764",
  "tenantAddress":     "Bangalore, India",
  "tenantType":        "TEMPORARY",
  "tenantStatus":      "PRESENT",
  "vacateDate":        "2026-12-31T00:00:00.000Z"
}
```

| Field              | Type      | Required | Validation                                       |
|--------------------|-----------|----------|--------------------------------------------------|
| `tenantName`       | string    | Yes      | Must not be blank                                |
| `tenantEmail`      | string    | Yes      | Valid email; must not belong to another tenant   |
| `tenantPhoneNumber`| string    | Yes      | Must not be blank                                |
| `tenantAddress`    | string    | No       | -                                                |
| `tenantType`       | enum      | Yes      | `PERMANENT` or `TEMPORARY`                       |
| `tenantStatus`     | enum      | Yes      | Any valid `TenantStatus` value                   |
| `vacateDate`       | timestamp | Conditional | Required and future date if `tenantType` = `TEMPORARY`; cleared automatically if `PERMANENT` |

**Responses**

| Status | Meaning           | Body         |
|--------|-------------------|--------------|
| `204`  | Updated           | Empty        |
| `400`  | Validation failed | Error object |
| `404`  | Tenant not found  | Error object |
| `409`  | Email conflict    | Error object |

---

### 5. Delete Tenant

```
DELETE /api/v1/tenants/{tenantId}
```

Permanently removes a tenant record.

**Path Parameters**

| Parameter  | Type   | Required | Example                                       |
|------------|--------|----------|-----------------------------------------------|
| `tenantId` | string | Yes      | `tenant-550e8400-e29b-41d4-a716-446655440000` |

**Responses**

| Status | Meaning          | Body         |
|--------|------------------|--------------|
| `204`  | Deleted          | Empty        |
| `404`  | Tenant not found | Error object |

---

## Business Rules

1. `tenantStatus` is set to `TO_BE_RESERVED` automatically on creation — cannot be set via create request.
2. `tenantType = PERMANENT` → `vacateDate` is always `null` (cleared on create and update).
3. `tenantType = TEMPORARY` → `vacateDate` is required and must be a future date.
4. `tenantEmail` is unique across all tenants. On update, a tenant may keep their own email but cannot take one that belongs to another tenant.
5. `tenantId` format: `tenant-<UUID>` — generated server-side, never client-supplied.

