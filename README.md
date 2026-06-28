# CV Analyzer
CV analyzer is a tool that helps you analyze and evaluate CVs (Curriculum Vitae) or resumes. It can provide insights into the strengths and weaknesses of a CV, suggest improvements, and help you create a more effective resume.

## Stack 
- Java
- Spring Boot
- Spring AI 
- MySQL(not yet implemented)

## Features
- Analyze CVs and provide feedback on formatting, content, and structure.
- Suggest improvements to enhance the overall quality of the CV.
- Provide insights into the strengths and weaknesses of the CV.
- Allow users to upload their CVs in various formats (PDF, DOCX, etc.) for analysis.
- Match CVs against job descriptions to assess suitability for specific roles.
- Generate a score or rating for the CV based on predefined criteria.
- Provide recommendations for optimizing the CV to increase chances of getting shortlisted.
- AI powered analysis for more accurate and personalized feedback.
- AI powered cover letter generation based on the CV and job description.

## Installation
1. Clone the repository:
   ```bash
   git clone   
   
    ```
2. Navigate to the project directory:
    ```bash
      cd cv-analyzer
      ```
   
3. Build the project using Maven:
    ```bash
      mvn clean install
      ```
4. Run the application:
    ```bash
      mvn spring-boot:run
      ```
5.Set up your OpenAI API key in the application.properties file:
    ```properties
      openai.api.key=YOUR_API_KEY
      ```
6.Set up your OpenAI Model in the application.properties file:
    ```properties
      openai.model=gpt-4
      ```

## Future Enhancements

- Integrate MySQL database for storing CVs and analysis results.
- Add user authentication and authorization for secure access to the application.
- Add support for additional file formats and languages for CV analysis.
- Implement machine learning algorithms to provide more accurate and personalized feedback on CVs.
- Add a feature to compare multiple CVs and provide a ranking based on suitability for specific job roles.