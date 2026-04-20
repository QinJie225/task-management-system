# Keycloak Configuration Guide

This document outlines the steps required to configure Keycloak for the internship project.

## 1. Accessing the Admin Console
- **URL**: [http://localhost:8080](http://localhost:8080)
- **Username**: `admin`
- **Password**: `admin`

---

## 2. Realm Setup
- Click the dropdown in the top-left corner and select **Create Realm**.
- **Realm name**: `internship-task-realm`
- Click **Create**.

---

## 3. Client Configuration

### **task-backend** (For Spring Boot)
- **Client ID**: `task-backend`
- **Client authentication**: `On`
- **Direct Access Grants**: `On`
- **Action**: Click **Save**, then go to the **Credentials** tab to copy the `Client Secret`.

### **task-frontend** (For React)
- **Client ID**: `task-frontend`
- **Client authentication**: `Off` (Public client)
- **Standard Flow**: `On`
- **Valid redirect URIs**: `http://localhost:3000/*`
- **Web origins**: `http://localhost:3000`
- **Action**: Click **Save**.

---

## 4. Roles
Navigate to **Realm Roles** → **Create role**:
1. `ADMIN`
2. `USER`

---

## 5. Demo Users

| Username | Password   | Assigned Role |
|:---------|:-----------|:--------------|
| `admin1` | `admin123` | `ADMIN`       |
| `user1`  | `user123`  | `USER`        |
| `ginger` | `ginger`   | N/A           |

### **Setup Instructions per User:**
1. **Create**: Go to **Users** → **Add user**, enter the username, and click **Create**.
2. **Password**: Go to the **Credentials** tab, click **Set password**, and toggle **Temporary** to `Off`.
3. **Role Mapping**: Go to the **Role mapping** tab, click **Assign role**, and select the appropriate role from the list.