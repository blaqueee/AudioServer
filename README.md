# AudioServer - Axelor Open Platform

## Description

AudioServer is a demo application designed to showcase the features and capabilities of the [Axelor Open Platform](https://axelor.com/open-platform/). While named "AudioServer," it appears to be based on the standard Axelor Open Platform demo and may not contain extensive custom audio-specific functionalities. The `modules/demo-audio` module, which might be intended for such features, could be a placeholder or under development.

This project serves as a practical example for developers looking to understand how to build applications using the Axelor platform.

## Features (Based on Axelor Open Platform Demo)

* Demonstrates core functionalities of the Axelor Open Platform.
* Provides a basic structure for an Axelor application.
* Includes examples of module integration (though specific audio features might be limited).
* User authentication and basic security setup.
* Example data models and views.

## Prerequisites

* **Java Development Kit (JDK):** Version 11 or higher.
* **Git:** For cloning the repository.
* **Gradle:** (Bundled with the project via Gradle Wrapper)

## Installation

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/blaqueee/AudioServer.git](https://github.com/blaqueee/AudioServer.git)
    cd AudioServer
    ```

2.  **Build the project:**
    This command will build the application and generate a `.war` file in the `build/libs` directory.
    ```bash
    ./gradlew -x test build
    ```
    *(On Windows, use `gradlew.bat -x test build`)*

## Running the Application

There are two main ways to run the application:

### 1. Using the Embedded Tomcat Server

This is the quickest way to get the application running.

1.  **Configure the database:**
    Edit the `src/main/resources/axelor-config.properties` file to set up your database connection (e.g., PostgreSQL).
    ```properties
    # Example for PostgreSQL
    db.default.driver = org.postgresql.Driver
    db.default.url = jdbc:postgresql://localhost:5432/your_database_name
    db.default.user = your_database_user
    db.default.password = your_database_password
    db.default.name = postgres # or your db name for some specific configs

    # Hibernate properties
    hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect
    hibernate.show_sql = true
    hibernate.hbm2ddl.auto = update # 'update' for development, 'validate' or none for production
    ```

2.  **Run the application:**
    ```bash
    ./gradlew --no-daemon run
    ```
    *(On Windows, use `gradlew.bat --no-daemon run`)*

    The application should be accessible at `http://localhost:8080/AudioServer` (the context path might vary, check Gradle output or default Axelor configuration if `/AudioServer` doesn't work, often it's the project name or as defined in `build.gradle`).

### 2. Deploying as a WAR file

1.  Build the project as described in the "Installation" section to generate the `.war` file (e.g., `AudioServer.war` or `demo.war` depending on the build configuration).
2.  Deploy the generated `.war` file to your standalone Tomcat server (version 8.5 or higher recommended) or any other compatible servlet container.

## Default Credentials

Once the application is running, you can log in using the default credentials:

* **Username:** `admin`
* **Password:** `admin`

## Development (Importing into Eclipse IDE)

You can import this project into Eclipse IDE for development:

### Option 1: Using Buildship Plugin

1.  Make sure you have the Buildship Gradle Integration plugin (version 2.1 or newer recommended) installed in Eclipse.
2.  Select `File > Import...`.
3.  Choose `Gradle > Existing Gradle Project`.
4.  Select the root directory of the cloned repository.
5.  Click `Finish`.

### Option 2: Generating Eclipse Project Files

1.  Navigate to the project's root directory in your terminal.
2.  Run the following Gradle command:
    ```bash
    ./gradlew eclipse
    ```
3.  In Eclipse, select `File > Import...`.
4.  Choose `General > Existing Projects into Workspace`.
5.  Select the root directory of the cloned repository.
6.  Click `Finish`.

To run the application from Eclipse, you can use the WTP (Web Tools Platform) tools and configure a Tomcat server within the IDE.

## Built With

* [Axelor Open Platform](https://axelor.com/open-platform/)
* Java 11
* Gradle
* Tomcat (for deployment)

## Contributing

Contributions are welcome! If you'd like to contribute, please fork the repository and submit a pull request. For major changes, please open an issue first to discuss what you would like to change.

*(As no specific contribution guidelines were found, standard GitHub practices are assumed.)*

## License

This project includes a `LICENSE` file. Please refer to the `LICENSE` file in the repository for details on the terms and conditions for use, reproduction, and distribution.