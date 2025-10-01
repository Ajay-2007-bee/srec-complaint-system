# Use an official Tomcat 10 image from Docker Hub
FROM tomcat:10.1-jdk17-temurin

# Remove the default Tomcat welcome page
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the WAR file you created into the Tomcat webapps folder
# We rename it to ROOT.war so it becomes the default app
COPY SRECMunicipalComplaintSystem.war /usr/local/tomcat/webapps/ROOT.war

# Expose port 8080 to the outside world
EXPOSE 8080

# The command to start the Tomcat server when the container launches
CMD ["catalina.sh", "run"]