library(gplots)
library(RColorBrewer)

# Note: plot jaccard similarity between two versions

jaccard <- function(x,y){return(length(intersect(x,y))/length(union(x,y)))}
jaccardMatr <- function(n, m){ 
  jmat = matrix(nrow = n, ncol = m)
  for (i in 1:n) {
    for (j in 1:m) {
      jmat[i, j] = jaccard(1:i, 1:j)
    }
  }
  return(jmat)
}

scores <- jaccardMatr(10,10)
scores <- round(scores, digits=2)

myPalette <- colorRampPalette(c("red", "yellow", "green"))(n = 299)

heatmap.2(scores,
          cellnote = scores,       # same data set for cell labels
          notecol="black",         # change font color of cell labels to black
          density.info="none",     # turns off density plot inside color legend
          trace="none",            # turns off trace lines inside the heat map
          margins=c(4,4),          # widens margins around plot
          col=myPalette,           # use on color palette defined earlier
          dendrogram="none",       # only draw a row dendrogram
          Rowv = FALSE,
          srtCol = 0,
          key = FALSE,             # disable legend
          xlab = "next version cardinality",
          ylab = "current version cardinality",
          Colv="NA")               # turn off column clustering
title("Jaccard scores matrix", line = -4, adj=0.62)
