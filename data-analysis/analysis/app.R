library(shiny)
library(ggplot2)
library(gridExtra)
library(ggpubr)
library(ggrepel)
source("signal-analysis.R")
source("correlation-analysis.R")

options(shiny.maxRequestSize = 60*1024^2)

ui <- fluidPage(
   
   titlePanel("Trend analysis: smell-generic characteristic evolution"),
   
   sidebarLayout(
      sidebarPanel(
         fileInput("dataset", "Choose a file", accept = c("text/csv","text/comma-separated-values,text/plain",".csv")),
         actionButton("calculate", "Calculate"),
         uiOutput("characteristicSelector")
      ),
      
      mainPanel(
         plotOutput("corPlot1"),
         plotOutput("trendPlot")
      )
   )
)

# Define server logic required to draw a histogram
server <- function(input, output) {
  data <- eventReactive(input$calculate, {read.csv(input$dataset$datapath)})
  
  output$trendPlot <- renderPlot({
    df <- data()
    print("computing")
    palette = "Dark2"
    
    signalType <- classifiableSignals[classifiableSignals$signal == input$characteristic, "type"]
    if (signalType != "generic") {
      df <- df[df$smellType == as.character(signalType), ]  
    }
  
    plots <- list()
    i <- 1
    df.sig <- classifySignal(df, input$characteristic)
    
    df.sig <- df.sig %>% group_by(classification) %>% add_tally()
    for (smellType in unique(df.sig$smellType)) {
      p <- ggplot(df.sig[df.sig$smellType==smellType,], aes(x="", group=classification, fill=classification)) + 
        geom_bar(width = 1, position = "stack") + coord_polar("y") + 
        labs(x = element_blank(), y = element_blank(), title = paste(smellType, "in all projects")) +
        scale_fill_brewer(palette=palette) +
        theme_minimal() +
        theme(plot.title = element_text(size=9))
      plots[[i]] <- p
      i <- i + 1
    }

    for (project in levels(df.sig$project)) {
      df.sig.p <- df.sig[df.sig$project == project,]

      for (smellType in unique(df.sig.p$smellType)) {
        p <- ggplot(df.sig.p[df.sig.p$smellType==smellType,], aes(x="", group=classification, fill=classification)) + 
          geom_bar(width = 1, position = "stack") + coord_polar("y") + 
          labs(x = element_blank(), y = element_blank(), title = paste(smellType, "in", project)) +
          scale_fill_brewer(palette=palette) +
          theme_minimal() +
          theme(plot.title = element_text(size=9))
        plots[[i]] <- p
        i <- i + 1
      }
      
    }

    print("completed")
    ggarrange(plotlist=plots, ncol = length(levels(df$smellType)), 
              nrow = length(levels(df$project)) + 1, common.legend = TRUE, 
              font.label = list(size=11)) + labs(title = paste("Trend evolution of '", input$characteristic, "'", sep = ""))
  }, 
  height = 300 * 5,
  res = 100)
  
  #output$projectSelector <- renderUI({
    #df <- data()
    #selectInput("inputProject", "Select a project", c(allProjects, levels(df$project)), selected = allProjects)
  #})
  
  output$characteristicSelector <- renderUI({
    df <- data()
    selectInput("characteristic", "Select a signal to classify", signalNames, selected = "size")
  })
  
  output$corPlot1 <- renderPlot({
    df <- data()
    
    signalType <- classifiableSignals[classifiableSignals$signal == input$characteristic, "type"]
    if (signalType != "generic") {
      df <- df[df$smellType == as.character(signalType), ]  
    }
    
    df.sig <- classifySignal(df, input$characteristic)
    
    df.icc <- data.frame()
    for (smellType in unique(df.sig$smellType)) {
      df.smell <- df.sig[df.sig$smellType == smellType,]
      icc <- computeCorrelMatrix(df.smell)
      icc$smellType <- smellType
      df.icc <- rbind(df.icc, icc)
    }
    ggplot(df.icc, aes(type, ICC), group=smellType, color=smellType, fill=smellType) + 
      geom_point(aes(size = p, color=smellType, fill=smellType), alpha=0.9) +
      geom_label_repel(aes(label = ifelse(p<=0.05, as.character(round(p, digits = 3)), "")),
                       box.padding   = 0.35, 
                       point.padding = 0.5,
                       segment.color = 'grey50') +
      labs(title = paste("ICC correlation analysis of", input$characteristic, "with signal classification")) +
      theme_classic()
  })
}

# Run the application 
shinyApp(ui = ui, server = server)

