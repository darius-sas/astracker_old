library(gplots)
library(RColorBrewer)
library(tidyr)
library(Matrix)

df <- read.csv("jaccard-3.1.2.csv")
df$jaccard <- as.numeric(df$jaccard)
s<- as.matrix(df %>% spread(nextId, jaccard, fill = 0))
s1<- apply(s[,8:ncol(s)], 2, as.numeric)

myPalette <- colorRampPalette(c("white", "blue", "green"))(n = 299)

heatmap.2(s1,
          #cellnote = s,  # same data set for cell labels
          main = "Jaccard scores", # heat map title
          notecol="black",      # change font color of cell labels to black
          density.info="density",  # turns off density plot inside color legend
          trace="none",         # turns off trace lines inside the heat map
          margins =c(5,5),     # widens margins around plot
          col=myPalette,        # use on color palette defined earlier
          dendrogram="none",    # only draw a row dendrogram
          Rowv = FALSE,
          Colv = FALSE,            # turn off column clustering
          labRow = df$curID,
          labCol = df$nextId,
          xlab = "Next",
          ylab = "Current")
# https://stackoverflow.com/questions/13087555/heatmap-in-r-how-to-resize-columns-labels