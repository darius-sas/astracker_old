library(ggplot2)

df <- read.csv("jaccard-3.1.3.csv")
df$jaccard <- as.numeric(df$jaccard)
df$curID <- as.character(df$curID)
df$nextId <- as.character(df$nextId)

base_size <- 12
# There is a diagonal of because the plotting function orders the data and arcan 
# detects the smells in a similar order from version to version
p <- ggplot(df, aes(curID, nextId)) +
  geom_tile(aes(fill = jaccard), colour = "gray10") + 
  scale_fill_gradient(low = "white", high = "firebrick") +
  theme_gray(base_size = base_size) +
  labs(x = "Current version smell ID", y = "Next version smell ID") +
  scale_x_discrete(expand = c(0, 0)) + scale_y_discrete(expand = c(0, 0)) +
  theme(axis.ticks = element_blank(),
        axis.text.x = element_text(size = base_size *.8, angle = 90),
        axis.text.y = element_text(size = base_size *.5))+
  geom_point(data = subset(df, match == "true"), colour="cyan3", shape=16, size=2, na.rm = T)

ggsave("similarity-score.pdf", width = 55, height = 35, units="cm")

  