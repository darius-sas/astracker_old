source("as-history-all/signal-analysis.R")
source("as-history-all/correlation-analysis.R")
source("as-history-all/survival-analysis.R")
library(ggplot2)
library(gridExtra)
library(ggpubr)
library(ggrepel)
library(reshape2)

## DESCRIPTIVE STATISTICS

#' Plots the counting of smells for each version and each project in the given data frame
#' @param df a data frame containing the data
plotSmellCountPerVersion <- function(df){
  df.tally <- df %>% group_by(project, versionPosition, smellType) %>% tally()
  ggplot(df.tally, aes(versionPosition, n, group = smellType, color = smellType)) + 
    geom_line() + 
    scale_y_sqrt() +
    theme(axis.text.x = element_text(angle=90, hjust = 1, vjust = 0.5)) +
    facet_wrap(~project, scales = "free") +
    labs(x = "Version number",
         y = "Number of smells",
         title = "Number of smells in the system by smell type",
         subtitle = "Square root scale")
}


#' Utility function plotting boxplots for each characteristic
ggboxplots<-function(df.melt){
  ggplot(df.melt, aes("", value, group = smellType, fill = smellType)) + 
    geom_boxplot() + 
    theme(axis.text.x = element_text(angle=90, hjust = 1)) +
    facet_wrap(project~variable, scales = "free", ncol = length(levels(df.melt$variable)) * 2)
}


#' Boxplots the values of each smell-generic characteristic
#' @param df a data frame containing the data
plotBoxplotsSmellGenericCharacteristics <- function(df){
  df.melt <- melt(df, c("project", "uniqueSmellID", "versionPosition", "smellType"), 
                  c("size", "overlapRatio", "pageRankMax", "pageRankAvrg"))
  ggboxplots(df.melt) + labs(title = "Boxplots of smell-generic characteristics by smell type")
}

plotBoxplotsCharacteristics <- function(df, characteristic, scales = "fixed"){
  df.melt <- melt(df, c("project", "uniqueSmellID", "versionPosition", "smellType"), characteristic)
  ggplot(df.melt, aes("", value, group = smellType, fill = smellType)) + 
    geom_boxplot() + 
    theme(axis.text.x = element_text(angle=90, hjust = 1)) +
    facet_wrap(~project, scales=scales) + 
    labs(title = paste("Boxplots of", characteristic, "by project"))
}

#' Boxplots the values of each smell-specific characteristic
#' @param df a data frame containing the data
plotBoxplotsSmellSpecificCharacteristics <- function(df){
  df.melt <- melt(df, c("project", "uniqueSmellID", "versionPosition", "smellType"), 
                  c("strength", "instabilityGap", "avrgEdgeWeight", "numOfEdges", 
                    "numOfInheritanceEdges"))
  ggboxplots(df.melt) + labs(title = "Boxplots of smell-specific characteristics by smell type")
}


#' Plots the counting of cycle shapes for each version and each project in the given data frame
#' @param df a data frame containing the data
plotCycleShapesCountPerVersion <- function(df){
  df.melt <- df %>% filter(smellType == "cyclicDep") %>%
    group_by(project, versionPosition, shape) %>%
    tally()
  ggplot(df.melt, aes(versionPosition, n, group = shape, color = shape)) + 
    geom_line() + 
    theme(axis.text.x = element_text(angle=90, hjust = 1)) +
    facet_wrap(~project, scales = "free")
}


## TREND ANALYSIS -- RQ1

#' Utility function to plot signal trends
#' @param df.sig Data frame containing the signal classification as returned by classifySignal()
ggplotsignaltrends <- function(df.sig, legend.position = "right", palette = "Paired", base.size = 12){
  df.sig <- df.sig %>% tally() %>% mutate(percentage = n/sum(n) * 100)
  df.sig$smellType <- plyr::revalue(df.sig$smellType, c("cyclicDep"="CD", "hubLikeDep"="HL", "unstableDep"="UD"))
  ggplot(df.sig, aes(x=smellType, y = percentage, group=classification, fill=classification)) + 
    geom_bar(stat="identity") +
    scale_fill_brewer(palette = palette) +
    theme_gray(base_size = base.size) +
    theme(axis.text.x = element_text(hjust = 1, vjust = 0.5), legend.position = legend.position)
}


#' Barplot of the trend classification for all projects combined
#' @param df.sig Data frame containing the signal classification as returned by classifySignal()
plotSignalTrendCharacteristicAllProjects <- function(df.sig, ...){
  characteristic <- unique(df.sig$characteristic)[1]
  df.grp <- df.sig %>% group_by(smellType, classification)
  ggplotsignaltrends(df.grp, ...) +
    theme(axis.text.x = element_text(hjust = 0.5), axis.title.y = element_text(angle = -90))+
    labs(x = "Smell types", y = "Percentage", 
         title = "Trend classification of all projects", 
         subtitle = paste("Characteristic:", characteristic)) + rotate()
}


#' Barplot of the trend classification for each project
#' @param df.sig Data frame containing the signal classification as returned by classifySignal()
plotSignalTrendCharacteristic <- function(df.sig, legend.position = "right", ...){
  characteristic <- unique(df.sig$characteristic)[1]
  df.grp <- df.sig %>% group_by(project, smellType, classification)
  ggplotsignaltrends(df.grp, legend.position = legend.position, ...) + 
    theme(axis.text.x = element_text(angle = 0, hjust = 0.5), axis.title.y = element_text(angle = -90)) +
    rotate() +
    facet_grid(project~.) +
    labs(x = "Smell types", y = "Percentage",
         title = "Trend classification per project",
         subtitle = paste("Characteristic:", characteristic))
}


#' Scatterplot of different correlation statistics
#' @param df.sig Data frame containing the signal classification as returned by classifySignal()
plotSignalTrendCorrelationWithAge <- function(df.sig){
  df.icc <- data.frame()
  characteristic <- unique(df.sig$characteristic)[1]
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
    theme_grey() +
    labs(x = "Correlation factor",
         y = "Correlation test",
         title = paste("ICC correlation analysis of", characteristic, "with signal classification"))
}


#' Line plot of the evolution of a given characteristic
#' @param df.sig Data frame containing the signal classification as returned by classifySignal()
#' @param characteristic The characteristic to plot
plotCharacteristicEvolutionTrend <- function(df, characteristic){
  ggplot(df, aes(versionPosition, !!sym(characteristic), group = uniqueSmellID, color = smellType)) + 
    geom_line() +  
    scale_y_sqrt() +
    facet_wrap(~project, scales = "free") +
    theme_grey() +
    labs(x = "Version Number",
         y = characteristic,
         title = paste("Evolution of", characteristic, "for each smell and for each project"),
         subtitle = "Square root scale")
}


#' Plots the ratio between the number of smells that have a p.value higher than 0.05 for
#' this correlation analysis.
#' @param df.corr The data frame resulting from the correlation analysis between the considered characteristics.
plotCharacteristicCorrelationValidity <- function(df.corr){
  df.corr.validity <- df.corr %>% mutate(isValid = p.value >= 0.05) %>% group_by(isValid) %>% tally()
  ggplot(df.corr.validity, aes(isValid, n)) + 
    geom_bar(aes(color = isValid, fill=isValid), stat = "identity") +
    labs(x = "Validity",
         y = "Count",
         title = "Number of statistically significant tests",
         subtitle = "p = 0.05")
}


#' Plots the estimates of correlation of the given correlation analysis data frame.
#' @param df.corr The data frame resulting from the correlation analysis between the considered characteristics.
plotCharacteristicCorrelationEstimates <- function(df.corr, base.size = 12){
  df.corr <- df.corr %>% filter(p.value <= 0.05)
  ggplot(df.corr, aes(project, estimate, group = var)) + 
    geom_jitter(aes(fill=smellType, color=smellType), height = 0.1, width = 0.2) +
    theme_gray(base_size = base.size) +
    theme(axis.text.x = element_text(angle = 90, hjust = 1, vjust = 0.5), legend.position = "none") +
    facet_grid(var~smellType, scales = "fixed") + 
    labs(x = "Project",
         y = "Correlation value",
         title = "Correlation among characteristics per project and smell type",
         subtitle = "Each smell is a point  (only p < 0.05)")
}


#' Plots both validity scores and correlation scores.
plotCharacteristicCorrelationBoth <- function(df.corr){
  gridExtra::grid.arrange(plotCharacteristicCorrelationValidity(df.corr),
                          plotCharacteristicCorrelationEstimates(df.corr))
}


#' Scatterplot of the values assumed by the given characteristic in the each version of the projects
#' @param df the data frame containing the values of the characteristic
#' @param characteristic The characteristic to plot
plotCharacteristicDistribution <- function(df, characteristic){
  ggplot(df, aes(age, !!sym(characteristic), group = smellType, colour = smellType)) +
    geom_point() +
    geom_jitter(width = 0.05, height = 0.05) +
    scale_x_continuous(breaks = pretty) +
    scale_y_continuous(breaks = pretty) +
    facet_wrap(~project, scales = "free_x") + 
    theme_grey() +
    labs(x = "Version number",
         y = paste(characteristic, "value"),
         title = paste("Distribution of", characteristic, "by age and smell type"),
         subtitle = "Each smell is a point")
}

## SURVIVAL ANALYSIS -- RQ2


#' Plot survival probabilities using the Kaplan-Meier statistic.
#' @param df the data frame containing the raw data
plotSurvivalProbabilities <- function(df, strata = "smellType", legend.position = "right", base.size = 12){
  surv <- computeSurvivalAnalysis(df, strata)
  ggsurvplot(surv$model, data = surv$data,
             facet.by = "project",
             short.panel.labs = T, ncol = 2,
             surv.median.line = "v") +
    scale_x_continuous(breaks = seq(0, 120, 5)) +
    theme_grey(base_size = base.size) +
    theme(legend.position = legend.position, axis.text.x = element_text(angle=90, hjust = 1, vjust = 0.5)) +
    labs(title = "Survival analysis by project")
}


#' Plot the age density of smells
#' @param df the data frame containing the raw data
plotAgeDensity <- function(df){
  df.unique <- df[!duplicated(df[,c("project", "uniqueSmellID")]), c("project", "uniqueSmellID", "age", "smellType")]
  ggplot(df.unique, aes(age, group = smellType, colour = smellType, fill = smellType)) +
    geom_density(alpha=0.2)+
    geom_vline(data = filter(df.unique, smellType == "cyclicDep"), aes(xintercept=median(age), color=smellType), linetype="dashed", size=1) +
    geom_vline(data = filter(df.unique, smellType == "hubLikeDep"), aes(xintercept=median(age), color=smellType), linetype="dashed", size=1) +
    geom_vline(data = filter(df.unique, smellType == "unstableDep"), aes(xintercept=median(age), color=smellType), linetype="dashed", size=1) +
    scale_x_continuous(breaks = pretty) +
    scale_y_continuous(breaks = pretty) +
    facet_wrap(~project, scales = "free") +
    theme_grey() +
    labs(x = "Age",
         y = "Smell density",
         title = "Age density distribution by smell type and project",
         subtitle = "Dashed lines represent the median for each group")
}


#' Plot the lifetime of each smell as a line
#' @param df the data frame containing the raw data
plotSmellLifetimeLines <- function(df){
  ggplot(df, aes(versionPosition, uniqueSmellID, group = uniqueSmellID, color = smellType)) + 
    geom_line() +  
    facet_wrap(~project, scales = "free") +
    theme_grey() +
    labs(x = "Version Number",
         y = "Unique Smell ID",
         title = "Smell lifetime per project and smell type")
}

## SAVING TO FILE

g_legend <- function(a.gplot){ 
  tmp <- ggplot_gtable(ggplot_build(a.gplot)) 
  leg <- which(sapply(tmp$grobs, function(x) x$name) == "guide-box") 
  legend <- tmp$grobs[[leg]] 
  return(legend)
} 

runAnalyses <- function(dataset.file){
  df <- read.csv(dataset.file)
  
  print("Running correlation analysis")
  corrSmellTypes = c("cyclicDep", "unstableDep")
  df.corr <- data.frame()
  for (smellType in corrSmellTypes) {
    df.tmp <- computeCharacteristicCorrelation(df, c("generic", smellType))
    df.tmp$smellType <- smellType
    df.corr <- bind_rows(df.corr, df.tmp)
  }
  
  print("Running signal analysis")
  df.sig <- data.frame()
  for(characteristic in unique(classifiableSignals$signal)){
    df.tmp<- classifySignal(df, characteristic)
    df.tmp$characteristic <- characteristic
    df.sig <- bind_rows(df.sig, df.tmp)
  }
  
  return(list(df = df, 
              corr = df.corr, 
              sig = df.sig))
}

saveResults <- function(datasets, dir = "."){
  write.csv(datasets$corr, "corr-analysis.csv")
  write.csv(datasets$sig,  "signal-analysis.csv")
}

loadResults <- function(dir = "."){
  df <- read.csv(file.path(dir, "dataset.csv"))
  df.sig <- read.csv(file.path(dir, "signal-analysis.csv"))
  df.corr <- read.csv(file.path(dir, "corr-analysis.csv"))
  return(list(df = df, corr = df.corr, sig = df.sig))
}

saveAllPlotsToFiles <- function(datasets, dir = "plots", format = "png", scale = 0.5, ...){
  dest <- file.path(dir)
  dir.create(dest, showWarnings = FALSE)

  df <- datasets$df
  df.corr.all <- datasets$corr
  df.sig.all <- datasets$sig
  
  # PRINT DESCRIPTIVE STATS
  plotSmellCountPerVersion(df)
  ggsave(paste("descriptive-smell-count.", format, sep = ""), path = dest, width = 20, height = 16, scale = scale)
  
  plotBoxplotsSmellGenericCharacteristics(df)
  ggsave(paste("descriptive-generic-charact.", format, sep = ""), path = dest, width = 20, height = 16, scale = scale)
  
  plotBoxplotsSmellSpecificCharacteristics(df) + theme_gray(base_size = 9)
  ggsave(paste("descriptive-specific-charact.", format, sep = ""), path = dest, width = 20, height = 16)
  
  plotCycleShapesCountPerVersion(df)
  ggsave(paste("descriptive-shape-count.", format, sep = ""), path = dest, width = 20, height = 16, scale = scale)
  
  
  # PRINT RQ2
  plotSurvivalProbabilities(df, legend.position = "top", base.size = 14)
  ggsave(paste("survival-probabilities.", format, sep = ""), path = dest, height = 12)
  plotSurvivalProbabilities(df %>% filter(affectedComponentType == "class"), legend.position = "top", base.size = 14) + labs (subtitle = "Class-smells only")
  ggsave(paste("survival-probabilities-class.", format, sep = ""), path = dest, height = 12)
  plotSurvivalProbabilities(df %>% filter(affectedComponentType == "package"), legend.position = "top", base.size = 14)  + labs (subtitle = "Package-smells only")
  ggsave(paste("survival-probabilities-packag.", format, sep = ""), path = dest, height = 12)
  
  plotSurvivalProbabilities(df %>% filter(smellType == "cyclicDep" & affectedComponentType == "class"), strata = "shape", legend.position = "top", base.size = 14)  + labs (subtitle = "Class-smells only")
  ggsave(paste("survival-probabilities-cycle-class.", format, sep = ""), path = dest, height = 12)
  plotSurvivalProbabilities(df %>% filter(smellType == "cyclicDep" & affectedComponentType == "package"), strata = "shape", legend.position = "top", base.size = 14)  + labs (subtitle = "Package-smells only")
  ggsave(paste("survival-probabilities-cycle-packag.", format, sep = ""), path = dest, height = 12)
  
  plotAgeDensity(df)
  ggsave(paste("survival-age-density.", format, sep = ""), path = dest, width = 20, height = 16)
  
  # PRINT RQ1 (LONGEST, LEAVE FOR LAST)
  for (smellType in unique(df.corr.all$smellType)) {
    df.corr <- df.corr.all %>% filter(smellType == smellType)
    plotCharacteristicCorrelationBoth(df.corr)
    ggsave(paste("correl-generic-", smellType, ".", format, sep = ""), path = dest, width = 15, height = 12)
  }
  #plotCharacteristicCorrelationEstimates(df.corr %>% filter(var == "pageRankMax~size"), 22) + theme(title=element_text(size=18))
  #ggsave(paste("correl-generic.", format, sep = ""), path = dest)
  
  for(charact in unique(df.sig.all$characteristic)){
    df.sig <- df.sig.all %>% filter(characteristic == charact)
    plotSignalTrendCharacteristic(df.sig, legend.position = "none", base.size=14)
    ggsave(paste("signal-trend-", charact, "-individual.", format, sep = ""), path = dest, height = 10.1, width = 5.83)
    plotSignalTrendCharacteristicAllProjects(df.sig, legend.position = "none", base.size=22)
    ggsave(paste("signal-trend-", charact, "-all-projects.", format, sep = ""), path = dest)
    plotSignalTrendCorrelationWithAge(df.sig)
    ggsave(paste("signal-corr-", charact, "-age.", format, sep = ""), path = dest)
    plotCharacteristicEvolutionTrend(df, charact)
    ggsave(paste("signal-evol-", charact, ".", format, sep=""), path = dest)
  }
}


