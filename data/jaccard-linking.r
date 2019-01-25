library(ggplot2)
library(stringr)

draw_similarity_score_plots <- function(data_file, base_size = 12){
  df <- read.csv(data_file)
  
  plots <- list()
  
  versions = levels(df$currentVersion);
  
  for (i in 1:(length(versions)-1)) {
    current_version = versions[i]
    next_version = versions[i+1]
    
    df.curr <- df[df$currentVersion == current_version & df$nextVersion == next_version,]
    
    ytitle = paste("Next version", next_version, "smell ID")
    xtitle = paste("Current version", current_version, "smell ID")
    
    # There is a diagonal of because the plotting function orders the data and arcan 
    # detects the smells in a similar order from version to version
    p <- ggplot(df.curr, aes(curID, nextId)) +
      geom_tile(aes(fill = similarityScore), colour = "gray10") + 
      scale_fill_gradient(low = "white", high = "firebrick") +
      theme_gray(base_size = base_size) +
      labs(x = xtitle, y = ytitle) +
      scale_x_discrete(expand = c(0, 0)) + scale_y_discrete(expand = c(0, 0)) +
      theme(axis.ticks = element_blank(),
            axis.text.x = element_text(size = base_size *.8, angle = 90),
            axis.text.y = element_text(size = base_size *.5))+
      geom_point(data = subset(df.cur, matched == "true"), colour="cyan3", shape=16, size=2, na.rm = T)
    
    plots[current_version] <- p
  }
  return(plots)
}
# TODO complete plotting, check version 3.0 antlr what's happening

files <- list.files(".", "*.csv")


for (i in 1:length(files)) {

  f = files[i]
  
  f_version <- str_extract(f, '[0-9]+\\.[0-9]+(\\.[0-9]+)?')
  c_version <- ""
  if (i > 1) {
    c_version <- str_extract(files[i - 1], '[0-9]+\\.[0-9]+(\\.[0-9]+)?')
  }
  
  print(paste("Reading file", f))
  getPlot(f)
  ggsave(paste('similarity-score-', f_version, '.pdf', sep = ''), width = 55, height = 35, units="cm")
}
