## Steps to Setup the Spring Boot Back end app (NCT Agriculture)

1. **Clone the application**

	```bash
	git clone https://gitlab.com/haitranviet96/home-iot-agriculture.git
	cd home-iot-agriculture
	```

2. **Create MySQL database**

	```bash
	create database ivofarm
	```
	
	+ Then, import database from `src/main/resources/database.sql` file.

3. **Change MySQL username and password as per your MySQL installation**

	+ open `src/main/com/agriculture/nct/util/AppConstants.java` file.

	+ change these properties as per your MySQL  installation
	```
	   public static final String MYSQL_USERNAME = "ivofarm";
    public static final String MYSQL_PASSWORD = "ivofarm";
    ```
    
4. **Run the app**

	You can run the spring boot app by typing the following command -

	```bash
	mvn spring-boot:run
	```

	The server will start on port 8080.
