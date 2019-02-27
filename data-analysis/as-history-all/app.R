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
         uiOutput("projectSelector"),
         uiOutput("characteristicSelector")
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
    
    df.sig <- classifySignal(df, input$characteristic)
    df.sig <- df.sig %>% group_by(classification) %>% add_tally()
    p1<-ggplot(df.sig[df.sig$smellType=="cyclicDep",], aes(x="", group=classification, fill=classification)) + 
        geom_bar(width = 1, position = "stack") + coord_polar("y") + 
        labs(title = "Trend analysis CD") +
        scale_fill_brewer(palette="Dark2") +
        theme_minimal()
    p2<-ggplot(df.sig[df.sig$smellType=="unstableDep",], aes(x="", group=classification, fill=classification)) + 
      geom_bar(width = 1, position = "stack") + coord_polar("y") + 
      labs(title = "Trend analysis UD") +
      scale_fill_brewer(palette="Dark2") +
      theme_minimal()
    p3<-ggplot(df.sig[df.sig$smellType=="hubLikeDep",], aes(x="", group=classification, fill=classification)) + 
        geom_bar(width = 1, position = "stack") + coord_polar("y") + 
        labs(title = "Trend analysis HL") +
        scale_fill_brewer(palette="Dark2") +
        theme_minimal()
    grid.arrange(p1, p2, p3, nrow = 2)
  })
  
  output$projectSelector <- renderUI({
    df <- data()
    selectInput("inputProject", "Select a project", c(allProjects, levels(df$project)), selected = allProjects)
  })
  
  output$characteristicSelector <- renderUI({
    selectInput("characteristic", "Select a characteristic", c("size", "pageRankMax", "overlapRatio"), selected = "size")
  })
}

# Run the application 
shinyApp(ui = ui, server = server)

