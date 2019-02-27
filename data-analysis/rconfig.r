
list.of.packages <- c("dplyr","ggplot2","gridExtra", "methods", "stringr", "RColorBrewer", "rmarkdown")
new.packages <- list.of.packages[!(list.of.packages %in% installed.packages()[,"Package"])]
if(length(new.packages)) install.packages(new.packages)