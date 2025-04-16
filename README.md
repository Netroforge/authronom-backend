[![slack](https://img.shields.io/badge/slack-join-brightgreen.svg?logo=slack)](https://join.slack.com/t/netroforge/shared_invite/zt-335byot5g-Z6PVCx45OgKKiTjJxz7odw)

# authronom-backend
The Single Sign-On Multi-Factor auth server backend

## Docker Compose Setup
The project uses Docker Compose for local development and deployment. The configuration is split into two files:

- `compose.yml` - Contains infrastructure services (PostgreSQL, Redis, Mailpit)
- `compose.app.yml` - Contains the application service (authronom-backend)

### Running Infrastructure Services Only
```bash
docker compose -f compose.yml up -d
```

### Running the Application with Infrastructure
```bash
docker compose -f compose.app.yml up -d
```

### Building and Running the Application
```bash
docker compose -f compose.app.yml build
docker compose -f compose.app.yml up -d
```

### Stopping All Services
```bash
docker compose -f compose.app.yml down
```

## Email change
We send confirmation code to the old and new,
so we follow best practices from OWASP:
https://owasp.org/www-community/pages/controls/Changing_Registered_Email_Address_For_An_Account
