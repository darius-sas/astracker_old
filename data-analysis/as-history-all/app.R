library(shiny)
library(ggplot2)
options(shiny.maxRequestSize = 30*1024^2)

# Define UI for application that draws a histogram
ui <- fluidPage(
   
   # Application title
   titlePanel("Trend analysis: smell-generic characteristic evolution"),
   
   # Sidebar with a slider input for number of bins 
   sidebarLayout(
      sidebarPanel(
         fileInput("dataset", "Choose a file", accept = c("text/csv","text/comma-separated-values,text/plain",".csv")),
         actionButton("calculate", "Calculate"),
         uiOutput("projectSelector")
      ),
      
      # Show a plot of the generated distribution
      mainPanel(
         plotOutput("trendPlot")
      )
   )
)

# Define server logic required to draw a histogram
server <- function(input, output) {
  allProjects <- "all"
  data <- eventReactive(input$calculate, {read.csv(input$dataset$datapath)})
  
  output$trendPlot <- renderPlot({
    df <- data()
    
    if (is.null(input$inputProject) | input$inputProject == allProjects) {
      projectsToSelect <- !as.logical(length(df))
    }else{
      projectsToSelect <- df$project == input$inputProject; 
    }
    df <- df[projectsToSelect,]
    df.unique <- df[!duplicated(df$uniqueSmellID), c(1,2,3,5)]
    ggplot(df.unique, aes(age, group = smellType, colour = smellType)) +
      geom_density()+
      geom_vline(aes(xintercept=mean(age)), color="blue", linetype="dashed", size=1) +
      scale_x_continuous(breaks = seq(1, length(df.unique$age), 1)) +
      scale_y_continuous(breaks = pretty)
  })
  
  output$projectSelector <- renderUI({
    df <- data()
    selectInput("inputProject", "Select a project", c(allProjects, levels(df$project)), selected = allProjects)
  })
}

# Run the application 
shinyApp(ui = ui, server = server)

