source("signal-analysis.R")
source("correlation-analysis.R")
source("survival-analysis.R")
library(ggplot2)
library(gridExtra)
library(ggpubr)
library(ggrepel)
library(reshape2)

## DESCRIPTIVE STATISTICS

#' Plots the counting of smells for each version and each project in the given data frame
#' @param df a data frame containing the data
plotSmellDensityPerVersion <- function(df, legend.position = "top", base.size = 12, ncol = 3, my.stat = "count"){
  df.tally <- df %>% group_by(project, versionPosition, smellType, affectedComponentType) %>% tally()
  df.unique <- unique(df[, c("project", "versionPosition", "nClasses", "nPackages")])
  df.tally <- left_join(df.tally, df.unique, by = c("project", "versionPosition")) %>%
    mutate(smell.type = as.factor(paste(affectedComponentType, smellType)),
           density = ifelse(affectedComponentType == "package", n / nPackages, n / nClasses))
  my.stat <- ifelse(my.stat == "count", sym("n"), sym("density"))
  ggplot(df.tally, aes(versionPosition, !!my.stat, group = smell.type, color = smellType)) + 
    geom_line(size=1.5, aes(linetype = affectedComponentType)) + 
    scale_y_continuous(breaks = pretty) +
    scale_x_continuous(breaks = pretty) +
    theme_gray(base_size = base.size) +
    theme(axis.text.x = element_text(hjust = 0.5, vjust = 0.5),
          axis.title.y = element_text(angle = -90),
          legend.text = element_text(size=14) ,
          legend.position = legend.position,
          legend.direction = "vertical") +
    facet_wrap(~project, scales = "free", ncol = ncol, strip.position = "right") +
    labs(x = "Version number",
         y = "Number of smells",
         title = paste("Smell", ifelse(my.stat == "n", "count", "density"), "in the systems")) +
    guides(color=guide_legend(ncol=2))
}

plotClassesPerPackageRatio <- function(df, base.size = 12, legend.position = "none"){
  df.sizes <- df %>% group_by(project, versionPosition) %>%
    select(project, versionPosition, nClasses, nPackages) %>%
    distinct()
  df.sizes$classesRatio <- df.sizes$nClasses / df.sizes$nPackages 
  ggplot(df.sizes, aes(x=versionPosition,y=classesRatio)) +
    geom_line() + facet_wrap(~project, scales = "free") +
    theme_grey(base_size = base.size) +
    labs(title="Classes per single package",
         x = "Versions",
         y = "Number of classes per package")
}

plotComponentCountPerVersion <- function(df, base.size = 12, legend.position = "none", type="packages"){
  df.sizes <- df %>% group_by(project, versionPosition) %>%
    select(project, versionPosition, nClasses, nPackages) %>%
    distinct()
  type.s <- sym(ifelse(type == "packages", "nPackages", "nClasses"))
  ggplot(df.sizes, aes(x=versionPosition,y=!!type.s)) +
    geom_line() + facet_wrap(~project, scales = "free") +
    theme_grey(base_size = base.size) +
    labs(title=paste("Number of", type, "per version"),
         x = "Versions",
         y = "Count")
}

plotSmellCountPerVersion <- function(df, smellTypeName, componentType = "class"){
  df.count <- df %>% filter(smellType == smellTypeName & affectedComponentType == componentType) %>%
    group_by(project, versionPosition) %>%
    tally()
  ggplot(df.count, aes(versionPosition, n)) + 
    geom_line() + 
    theme(axis.text.x = element_text(angle=90, hjust = 1)) +
    facet_wrap(~project, scales = "free") +
    labs(x="Versions", y="Count", title = paste("Number of ", smellTypeName, "on", componentType))
}

plotCorrAnalysisCount <- function(df, method = "pearson"){
  df.tally <- df %>% group_by(project, versionPosition, smellType, affectedComponentType) %>%
    tally() %>%
    mutate(type = paste(smellType, affectedComponentType, sep = "."))
  df.sizes <- df %>% group_by(project, versionPosition) %>%
    select(project, versionPosition, nClasses, nPackages) %>%
    distinct()
  df.tally <- data.frame(df.tally)
  df.tally$smellType<-NULL
  df.tally$affectedComponentType<-NULL
  df.tally <- tidyr::spread(df.tally, key = type, value = n, fill=0)
  df.corr <- left_join(df.sizes, df.tally, by = c("project", "versionPosition")) %>%
    arrange(project, versionPosition)
  
  plots<-list()
  for (project in levels(df.corr$project)) {
    p <- GGally::ggcorr(df.corr[df.corr$project == project,], 
                         method = c("pairwise", method),
                         label = T, angle=90, hjust=0.25) + labs(title = project)
    plots[[project]] <- p
  }
  return(plots)
}

#' Utility function plotting boxplots for each characteristic
ggboxplots<-function(df.melt){
  ggplot(df.melt, aes("", value, 
                      group = interaction(smellType, affectedComponentType), 
                      fill = interaction(smellType, affectedComponentType))) + 
    geom_boxplot(outlier.shape = NA) + 
    theme(axis.text.x = element_text(angle=90, hjust = 1)) +
    facet_wrap(project~variable, scales = "free", ncol = length(levels(df.melt$variable)) * 2) +
    labs(fill = "Smells")
}


#' Boxplots the values of each smell-generic characteristic
#' @param df a data frame containing the data
plotBoxplotsSmellGenericCharacteristics <- function(df){
  df.melt <- melt(df, c("project", "uniqueSmellID", "versionPosition", "smellType", "affectedComponentType"), 
                  c("size", "overlapRatio", "pageRankAvrg", "pageRankWeighted"))
  ggboxplots(df.melt) + labs(title = "Boxplots of smell-generic characteristics by smell type")
}

plotBoxplotsCharacteristics <- function(df, characteristic, scales = "fixed", base.size = 14, legend.position = "top"){
  df.melt <- melt(df, c("project", "uniqueSmellID", "versionPosition", "smellType"), characteristic)
  ggplot(df.melt, aes("", value, group = smellType, fill = smellType)) + 
    geom_boxplot()+ rotate()+
    theme_gray(base_size = base.size) +
    theme(axis.title = element_blank(), legend.position = legend.position) +
    facet_grid(project~variable, scales=scales)
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

plotAffectedDesignBarplot <- function(df){
  df <- df %>% filter(smellType == "cyclicDep") %>%
    group_by(project, affectedDesignLevel) %>%
    tally()
  ggplot(df, aes(x=affectedDesignLevel, y = n, fill = affectedDesignLevel)) + 
    geom_bar(stat="identity") + 
    scale_y_log10() +
    theme_gray() +
    theme(legend.position = "top", axis.text.x = element_blank()) +
    facet_wrap(~project, scales="free") + 
    ylab("count (log scale)")
}

plotAffectedDesignChangedCount <- function(df, showChange = T, palette = "Set1", base.size = 14){
  fun <- ifelse(showChange, affectedDesignChange, function(x){length(unique(as.numeric(x))) != 1})
  df.count <- df %>% filter(smellType == "cyclicDep") %>%
    group_by(project, uniqueSmellID) %>%
    summarise(hasChanged = fun(affectedDesignLevel)) %>% 
    group_by(project, hasChanged) %>% 
    tally() %>% mutate(perc = n / sum(n) * 100)
  
  ggplot(df.count, aes(x=project, y = perc, fill = hasChanged)) + 
    geom_bar(stat="identity", position = position_stack()) +
    theme_bw(base_size = base.size) +
    scale_x_discrete(expand=c(0,0)) +
    scale_y_continuous(expand=c(0,0)) +
    scale_fill_brewer(palette = palette) +
    theme(axis.text.x = element_text(angle = -90, hjust = 0, vjust = 0.5),axis.title.x = element_blank(), 
          axis.ticks.x = element_blank(), axis.title.y = element_text(angle = -90),
          legend.position = "top", legend.title = element_blank(),
          legend.spacing.x = unit(.05, 'inch'),
          panel.grid = element_blank(), panel.border = element_blank()) +
    #facet_grid(~project, scales="free") + 
    labs(title = "CD smells that have changed their affected design level",
         subtitle = "In percentage", y = "Percentage") +
    guides(fill=guide_legend(ncol=3))
}

affectedDesignChange <- function(affectedDesign){
  if (length(unique(as.numeric(affectedDesign))) != 1) {
    start <- affectedDesign[1]
    end <- NULL;
    for (a in affectedDesign) {
      if(a != start){
        end = a
      }
    }
    return(paste(start, "->", end, sep=""))
  }
  return(paste(affectedDesign[1], "->", affectedDesign[length(affectedDesign)], sep=""))
}

## TREND ANALYSIS -- RQ1

#' Utility function to plot signal trends
#' @param df.sig Data frame containing the signal classification as returned by classifySignal()
ggplotsignaltrends <- function(df.sig, legend.position = "right", palette = "Paired", base.size = 12){
  df.sig <- df.sig %>% tally() %>% mutate(percentage = n/sum(n) * 100)
  df.sig$smellType <- plyr::revalue(df.sig$smellType, c("cyclicDep"="CD", "hubLikeDep"="HL", "unstableDep"="UD"))
  ggplot(df.sig, aes(x=interaction(smellType, affectedComponentType), y = percentage, group=classification, fill=classification)) + 
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


plotSignalTrendCharacteristicAllProjectsOnePlot <- function(df.sig, legend.position = "top", 
                                                            filter = c("size", "numOfEdges", "pageRankMax", "pageRankWeighted"), ...){
  df.grp <- df.sig %>% filter(characteristic %in% filter) %>% 
    group_by(characteristic, smellType, affectedComponentType, classification)
  ggplotsignaltrends(df.grp, legend.position = legend.position, ...) + 
    theme(axis.text.x = element_text(angle = 0, hjust = 0.5), axis.title.y = element_text(angle = -90)) +
    rotate() +
    facet_grid(characteristic~.) +
    labs(x = "Smell types", y = "Percentage",
         title = "Trend classification all projects")
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
plotCharacteristicCorrelationValidity <- function(df.corr, base.size){
  df.corr.validity <- df.corr %>% mutate(isValid = p.value >= 0.05) %>% group_by(isValid) %>% tally()
  ggplot(df.corr.validity, aes(isValid, n)) + 
    geom_bar(aes(color = isValid, fill=isValid), stat = "identity") +
    theme_gray(base_size = base.size) +
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
    theme_gray(base_size = base.size) + rotate() +
    theme(axis.text.x = element_text(angle = 0, hjust = 0.5, vjust = 0.5), legend.position = "none",
          axis.title.y = element_text(angle = -90)) +
    facet_grid(smellType~var, scales = "fixed") + 
    labs(x = "Project",
         y = "Correlation value",
         title = "Correlation among characteristics",
         subtitle = "Each smell is a point (only p < 0.05)")
}


#' Plots both validity scores and correlation scores.
plotCharacteristicCorrelationBoth <- function(df.corr, base.size = 12){
  gridExtra::grid.arrange(plotCharacteristicCorrelationValidity(df.corr, base.size),
                          plotCharacteristicCorrelationEstimates(df.corr, base.size))
}

plotCharacteristCorrelationBoxplots <- function(df.corr, base.size = 12, legend.position = "none"){
  df.corr.f <- df.corr %>% filter(p.value <= 0.05)
  df.corr.f$smellType <- plyr::revalue(df.corr.f$smellType, c("cyclicDep"="CD", "hubLikeDep"="HL", "unstableDep"="UD"))
  ggplot(df.corr.f, aes(interaction(smellType, affectedComponentType), estimate, fill = interaction(smellType, affectedComponentType))) + 
    geom_boxplot() + rotate() +
    theme_gray(base_size = base.size) +
    theme(legend.position = legend.position, axis.title.y = element_blank()) +
    facet_wrap(~var, scales = "free_x") +
    labs(y = "Pearson's correlation")
  
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
plotSurvivalProbabilitiesOld <- function(df, strata = "smellType", legend.position = "right", base.size = 12){
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

plotSurvivalProbabilities <- function(df, strata = "smellType", base.size = 12, title = "Smell Types", 
                                      arrange.layout = "h"){
  i <- 1
  plist <- list()
  for (proj in unique(df$project)) {
    df.tmp <- df %>% filter(project == proj)
    surv <- computeSurvivalAnalysis(df.tmp, strata)
    plist[[i]] <- ggsurvplot(surv$model, data = surv$data,
                             surv.median.line = "v", short.panel.labs = T,
                             ggtheme = theme_grey(base_size = base.size),
                             title = "", facet.by = "project") +
                 facet_wrap(~project, strip.position = "right") +
                 theme(legend.position = "none",
                       axis.title = element_blank(), panel.border = element_blank(),
                       plot.margin=unit(c(-0.5, 0,-.08, 0.2), "cm"))
    if(i == 1) {
      nCol = ifelse(arrange.layout == "h", 2, 3)
      p <- plist[[i]] + theme(legend.position = "top") + guides(color=guide_legend(ncol = nCol, title = title))
      legend <- get_legend(p)
    }
    i <- i + 1
  }
  plist[[i]] <- legend
  if(arrange.layout == "h"){
    arrange.layout <- rbind(1:4, 5:8, 9:12, c(13, 14, 15, 15))
  }else{
    #arrange.layout <- rbind(c(15,15), 1:2, 3:4, 5:6, 7:8, 9:10, 11:12, 13:14)
    arrange.layout <- rbind(c(15,15), 1:2, 3:4)
  }
  grid.arrange(grobs = plist, layout_matrix = arrange.layout, bottom="Versions", 
               heights=unit(append(0.7,replicate(nrow(arrange.layout)-1, 2)), "in"))
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
  df.tmp <- computeCharacteristicCorrelation(df, c("generic"))
  levels(df.tmp$smellType) <- c(levels(df.tmp$smellType), "all")
  df.tmp$smellType <- "all"
  df.corr <- bind_rows(df.corr, df.tmp)
  
  print("Running signal analysis")
  df.sig <- data.frame()
  library(foreach)
  for(characteristic in classifiableSignals$signal){
    df.tmp<- classifySignal(df, characteristic)
    df.tmp$characteristic <- characteristic
    df.sig <- bind_rows(df.sig, df.tmp)
  }
  
  return(list(df = df, 
          corr = df.corr, 
          sig = df.sig))
}

saveResults <- function(datasets, dir = "."){
  write.csv(datasets$corr, "corr-analysis.csv", row.names = F)
  write.csv(datasets$sig,  "signal-analysis.csv", row.names = F)
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
  p <- shift_legend(plotSmellDensityPerVersion(df, base.size = 14, ncol = 4, my.stat = "density"))
  ggsave(paste("descriptive-smell-density.", format, sep = ""), path = dest, height = 8,  width = 15, plot = p)
  
  plotSmellDensityPerVersion(df, base.size = 14, ncol = 4, my.stat = "count") + scale_y_sqrt() + labs(subtitle = "Square root scale")
  ggsave(paste("descriptive-smell-count.", format, sep = ""), path = dest, height = 8,  width = 15)
  
  plotBoxplotsSmellGenericCharacteristics(df)
  ggsave(paste("descriptive-generic-charact.", format, sep = ""), path = dest, width = 20, height = 16, scale = 0.9)
  
  plotBoxplotsSmellSpecificCharacteristics(df) + theme_gray(base_size = 9)
  ggsave(paste("descriptive-specific-charact.", format, sep = ""), path = dest, width = 20, height = 16)
  
  plotCycleShapesCountPerVersion(df)
  ggsave(paste("descriptive-shape-count.", format, sep = ""), path = dest, width = 20, height = 16, scale = scale)
  
  plotAffectedDesignChangedCount(df, showChange = T) 
  ggsave(paste("descriptive-affected-design.", format, sep = ""), path = dest)
  
  plotClassesPerPackageRatio(df, base.size = 10)
  ggsave(paste("descriptive-classes-per-package.", format, sep = ""), path = dest)
  
  plotComponentCountPerVersion(df, type="packages")
  ggsave(paste("descriptive-count-packages.", format, sep = ""), path = dest)
  plotComponentCountPerVersion(df, type="classes")
  ggsave(paste("descriptive-count-classes.", format, sep = ""), path = dest)
  
  plotSmellCountPerVersion(df, "cyclicDep", "class")
  ggsave(paste("descriptive-count-cd-class.", format, sep = ""), path = dest)
  plotSmellCountPerVersion(df, "cyclicDep", "package")
  ggsave(paste("descriptive-count-cd-package.", format, sep = ""), path = dest)
  plotSmellCountPerVersion(df, "hubLikeDep", "package")
  ggsave(paste("descriptive-count-hl-package.", format, sep = ""), path = dest)
  plotSmellCountPerVersion(df, "hubLikeDep", "class")
  ggsave(paste("descriptive-count-hl-class.", format, sep = ""), path = dest)
  plotSmellCountPerVersion(df, "unstableDep", "package")
  ggsave(paste("descriptive-count-ud-package.", format, sep = ""), path = dest)
  
  plots<- plotCorrAnalysisCount(df)
  for (project in names(plots)) {
    ggsave(paste("descriptive-count-corr-", project, ".", format, sep=""), plot = plots[[project]], path = dest)
  }
  
  # PRINT RQ2
  df <- df %>% mutate(smellTypeGeneral = paste(smellType, affectedComponentType))
  p <- plotSurvivalProbabilities(df, strata = "smellTypeGeneral", base.size = 14, arrange.layout = "v")
  ggsave(paste("survival-probabilities.", format, sep = ""), path = dest, height = 12, plot = p)
  p2 <- plotSurvivalProbabilities(df %>% filter(smellType == "cyclicDep"), strata = "shape", 
                                 base.size = 14, title = "Shapes", arrange.layout = "v")
  ggsave(paste("survival-probabilities-shapes.", format, sep = ""), path = dest, height = 12, plot = p2)
  
  plotAgeDensity(df)
  ggsave(paste("survival-age-density.", format, sep = ""), path = dest, width = 20, height = 16)
  
  # PRINT RQ1 (LONGEST, LEAVE FOR LAST)
  for (smellType in unique(df.corr.all$smellType)) {
    df.corr <- df.corr.all %>% filter(smellType == smellType)
    plotCharacteristicCorrelationBoth(df.corr, base.size=14)
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
  plotSignalTrendCharacteristicAllProjectsOnePlot(df.sig, base.size = 14)
  ggsave(paste("signal-trend-all-projects-oneplot.", format, sep = ""), path = dest, height = 10.1, width = 5.83)
}


library(gtable)
library(cowplot)

shift_legend <- function(p){
  
  # check if p is a valid object
  if(!"gtable" %in% class(p)){
    if("ggplot" %in% class(p)){
      gp <- ggplotGrob(p) # convert to grob
    } else {
      message("This is neither a ggplot object nor a grob generated from ggplotGrob. Returning original plot.")
      return(p)
    }
  } else {
    gp <- p
  }
  
  # check for unfilled facet panels
  facet.panels <- grep("^panel", gp[["layout"]][["name"]])
  empty.facet.panels <- sapply(facet.panels, function(i) "zeroGrob" %in% class(gp[["grobs"]][[i]]))
  empty.facet.panels <- facet.panels[empty.facet.panels]
  if(length(empty.facet.panels) == 0){
    message("There are no unfilled facet panels to shift legend into. Returning original plot.")
    return(p)
  }
  
  # establish extent of unfilled facet panels (including any axis cells in between)
  empty.facet.panels <- gp[["layout"]][empty.facet.panels, ]
  empty.facet.panels <- list(min(empty.facet.panels[["t"]]), min(empty.facet.panels[["l"]]),
                             max(empty.facet.panels[["b"]]), max(empty.facet.panels[["r"]]))
  names(empty.facet.panels) <- c("t", "l", "b", "r")
  
  # extract legend & copy over to location of unfilled facet panels
  guide.grob <- which(gp[["layout"]][["name"]] == "guide-box")
  if(length(guide.grob) == 0){
    message("There is no legend present. Returning original plot.")
    return(p)
  }
  gp <- gtable_add_grob(x = gp,
                        grobs = gp[["grobs"]][[guide.grob]],
                        t = empty.facet.panels[["t"]],
                        l = empty.facet.panels[["l"]],
                        b = empty.facet.panels[["b"]],
                        r = empty.facet.panels[["r"]],
                        name = "new-guide-box")
  
  # squash the original guide box's row / column (whichever applicable)
  # & empty its cell
  guide.grob <- gp[["layout"]][guide.grob, ]
  if(guide.grob[["l"]] == guide.grob[["r"]]){
    gp <- gtable_squash_cols(gp, cols = guide.grob[["l"]])
  }
  if(guide.grob[["t"]] == guide.grob[["b"]]){
    gp <- gtable_squash_rows(gp, rows = guide.grob[["t"]])
  }
  gp <- gtable_remove_grobs(gp, "guide-box")
  
  return(gp)
}


g_legend <- function(a.gplot){ 
  tmp <- ggplot_gtable(ggplot_build(a.gplot)) 
  leg <- which(sapply(tmp$grobs, function(x) x$name) == "guide-box") 
  legend <- tmp$grobs[[leg]] 
  return(legend)
} 

