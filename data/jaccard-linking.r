library(gplots)
library(RColorBrewer)
library(tidyr)

df <- read.csv("jaccard-17.csv")
df$jaccard <- as.numeric(df$jaccard)
s<- as.data.frame(df %>% spread(next., jaccard))
s1<- round(as.matrix(s[,2:ncol(s)]), digits = 2)

myPalette <- colorRampPalette(c("red", "yellow", "green"))(n = 299)
dev.new(width=20, height=20,)
heatmap.2(s1,
          #cellnote = s,  # same data set for cell labels
          main = "Jaccard scores", # heat map title
          notecol="black",      # change font color of cell labels to black
          density.info="none",  # turns off density plot inside color legend
          trace="none",         # turns off trace lines inside the heat map
          margins =c(20,20),     # widens margins around plot
          col=myPalette,        # use on color palette defined earlier
          dendrogram="none",    # only draw a row dendrogram
          Rowv = FALSE,
          Colv="NA",            # turn off column clustering
          labRow = s[,1])
# https://stackoverflow.com/questions/13087555/heatmap-in-r-how-to-resize-columns-labels