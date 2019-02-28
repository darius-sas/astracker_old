library(shiny)
library(ggplot2)
library(gridExtra)
library(ggpubr)
source("analysis.r")

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
         #uiOutput("projectSelector"),
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
    print("computing")
    palette = "Dark2"
    
    plots <- list()
    i <- 1
    df.sig <- classifySignal(df, input$characteristic)
    df.sig <- df.sig %>% group_by(classification) %>% add_tally()
    for (smellType in levels(df.sig$smellType)) {
      p <- ggplot(df.sig[df.sig$smellType==smellType,], aes(x="", group=classification, fill=classification)) + 
        geom_bar(width = 1, position = "stack") + coord_polar("y") + 
        labs(x = element_blank(), y = element_blank(), title = "All projects") +
        scale_fill_brewer(palette=palette) +
        theme_minimal() +
        theme(plot.title = element_text(size=9))
      plots[[i]] <- p
      i <- i + 1
    }

    for (project in levels(df$project)) {
      df.sig <- classifySignal(df[df$project==project,], input$characteristic)
      df.sig <- df.sig %>% group_by(classification) %>% add_tally()
      
      for (smellType in levels(df$smellType)) {
        p <- ggplot(df.sig[df.sig$smellType==smellType,], aes(x="", group=classification, fill=classification)) + 
          geom_bar(width = 1, position = "stack") + coord_polar("y") + 
          labs(x = element_blank(), y = element_blank(), title = project) +
          scale_fill_brewer(palette=palette) +
          theme_minimal() +
          theme(plot.title = element_text(size=9))
        plots[[i]] <- p
        i <- i + 1
      }
      
    }
    df.sig <- classifySignal(df, input$characteristic)
    df.sig <- df.sig %>% group_by(classification) %>% add_tally()
    print("completed")
    ggarrange(plotlist=plots, ncol = length(levels(df$smellType)), 
              nrow = length(levels(df$project)) + 1, common.legend = TRUE, labels=levels(df$smellType), font.label = list(size=11))
  }, 
  height = 300 * 5,
  res = 100)
  
  #output$projectSelector <- renderUI({
    #df <- data()
    #selectInput("inputProject", "Select a project", c(allProjects, levels(df$project)), selected = allProjects)
  #})
  
  output$characteristicSelector <- renderUI({
    selectInput("characteristic", "Select a characteristic", c("size", "pageRankMax", "overlapRatio"), selected = "size")
  })
}

# Run the application 
shinyApp(ui = ui, server = server)

