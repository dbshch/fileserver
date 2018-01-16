### File Server
![File Server UI](ui.png)

### Requirement Specifications
- multiple 
- simultaneous
- stable 
- resumable

### Architecture
![Architecture](arch.png)

### Manage and build
- maven
    - build a jar: `mvn clean package -DskipTests`

### Deployment
![Deployment](deploy.png)
- Configure the API token for authentication
    - Modify application.properties annotated with `## token` 
- Configure the database
    - Modify application.properties annotated with `## msserver sql`
- Configure Web Server interfaces
    - Modify application.yml annotated with `#UploadWatcher parameters`
- Configure logging level
    - Modify application.properties annotated with `## logging`

### Example of coordination with another web server.
![Coordination](coordinate.png)

### ER diagram
![ER diagram](tables.png)
