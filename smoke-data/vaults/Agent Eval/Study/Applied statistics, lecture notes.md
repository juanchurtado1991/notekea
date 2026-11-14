---
course: Applied statistics
lecturer: Dr. Mónica Salas
tags: [course, statistics]
---
# Applied statistics, lecture notes

Evening course at the Universidad Nacional, Thursdays 18:00 to 21:00 in room B-204, from 20 August to 10 December 2026. Lecturer: Dr. Mónica Salas. I study with [[Laura Castro]].

Grade: homework 30 %, midterm 30 %, final exam 40 %. The examples come from coffee, because half of us work in it.

## Week 1. Data and variables (20 August)

Kinds of variables: categorical (the variety of a coffee plant), ordinal (a cup score band), discrete (beans with defects in a sample) and continuous (the weight of a delivery). The kind of a variable decides which summaries and which tests make sense. A sample is the part we measure; the population is what we want to talk about.

Dr. Salas: "Before any number, ask who was measured, how, and who was left out."

Homework 1 (10 points), due 27 August: classify the 20 variables of the cooperative's delivery form.

## Week 2. Describing data (27 August)

Mean, median, mode; range, interquartile range, standard deviation. The median resists outliers; the mean does not. Example: delivery weights of 10 farms, in kg: 120, 135, 140, 150, 155, 160, 170, 180, 190 and 900. Mean 230 kg, median 157.5 kg: the one large farm pulls the mean far from the typical farm.

Boxplots show the median, the quartiles and the outliers at a glance. Always plot before summarizing.

Homework 2 (10 points), due 3 September: summaries and a boxplot of the 2025 deliveries.

## Week 3. Probability (3 September)

Events, complements, the addition and multiplication rules, conditional probability. Independence is an assumption, not a fact: two farms on the same road are not independent when the road closes. Tree diagrams for problems in two steps.

Example: 8 % of lots fail the moisture check; of those, 60 % come from farms above 1,400 m. Worked out with a tree, as a table of counts out of 1,000 lots, which is easier to read.

Homework 3 (10 points), due 10 September: six problems from chapter 4.

## Week 4. The normal distribution (10 September)

The bell curve, described by its mean and standard deviation. About 68 % of values lie within one standard deviation of the mean, 95 % within two, 99.7 % within three. A z-score says how many standard deviations a value is from the mean: z = (x − mean) / sd.

Example: cup scores with mean 82 and sd 2.5; a score of 87 has z = 2, better than about 97.7 % of lots.

Homework 4 (15 points), due 17 September: z-scores and areas under the curve.

## Week 5. Sampling and confidence intervals (17 September)

The sample mean varies from sample to sample; its standard error is sd / √n. A confidence interval is the estimate plus or minus a margin. For a 95 % interval with a large sample we use z = 1.96; for 90 %, 1.645; for 99 %, 2.576. With small samples we use the t distribution instead (week 8).

Example: 64 cherries weighed, mean 1.72 g, sd 0.24 g. Standard error 0.03 g, margin 1.96 × 0.03 = 0.059 g, interval 1.66 to 1.78 g.

"95 % is about the method, not about this one interval."

Homework 5 (15 points), due 24 September: intervals for the moisture data.

## Week 6. Hypothesis tests (24 September)

Null and alternative hypotheses; the p-value is the probability of data at least this extreme if the null were true. A small p-value is evidence against the null, not proof of the alternative. Errors of type I and type II; significance level 0.05 by convention, not by law.

The coffee-tasting example: 24 tasters, each given a washed and a natural coffee in random order, asked which they preferred. 17 of 24 chose the washed one. Under the null (no preference, p = 0.5) a result this extreme has a one-sided p-value of 0.03 (we had said beforehand that the washed coffee would win), so at the 0.05 level we reject the null. Dr. Salas made us run it in class with real cups; I chose the natural.

Homework 6 (15 points), due 1 October: four tests, with the reasoning written out.

## Week 7. Midterm (1 October)

The midterm covered weeks 1 to 6, 30 % of the grade. Two hours, one calculator, no notes. I got 82 out of 100; lost most points on the conditional probability question.

## Week 8. Comparing two means (8 October)

The t-test for two independent samples, and the paired t-test when the same units are measured twice. Check the spread of both groups before choosing the version of the test. A confidence interval for the difference says more than the p-value alone.

Example: yields of 12 plots under shade and 12 in full sun. Mean difference 0.4 t/ha in favour of shade, p = 0.21: no clear difference with this few plots.

Homework 7 (20 points), due 15 October: the shade data with the paired design.

## Week 9. Chi-square tests (15 October)

For counts in categories. Test of independence in a two-way table; expected counts are row total × column total / grand total, and every expected count should be at least 5.

Example: defective beans by drying method, in samples of 300 beans each from patios, raised beds and mechanical dryers. Chi-square 9.8 with 2 degrees of freedom, p = 0.007: the drying method and the share of defects are related.

Homework 8 (20 points), due 22 October: the cooperative's quality grades by district.

## Week 10. Correlation and simple regression (22 October)

The correlation r measures how close points are to a straight line, from −1 to 1; it says nothing about cause. Least squares regression: y = a + b·x, where b is the change in y for one unit more of x. Residuals should look like noise; a pattern in them means the line is the wrong shape.

Example: altitude and cup score of 40 farms. r = 0.62; slope 0.8 points of cup score per 100 m of altitude. R² = 0.38, so altitude explains about 38 % of the variation in score.

Homework 9 (20 points), due 29 October: regression of yield on rainfall.

## Week 11. ANOVA (29 October)

Analysis of variance compares the means of three or more groups at once, instead of many t-tests that would inflate the type I error. The F statistic is the variance between groups over the variance within them. If the F-test is significant, a post-hoc test (Tukey) says which groups differ.

Dataset: the Coto Brus yields, 36 plots under three fertilizers, 12 plots each. F = 5.4, p = 0.009. Tukey: fertilizer C beats A; B is not clearly different from either.

Homework 10 (25 points), due 12 November: one-way ANOVA on the Coto Brus yields with a fourth fertilizer added.

## Week 12. Multiple regression (5 November)

Several predictors at once: y = a + b1·x1 + b2·x2 + … Each coefficient is the change in y for one unit more of its predictor, with the others held fixed. Adjusted R² penalizes predictors that add little. Beware of predictors that are strongly correlated with each other.

Example: cup score on altitude and shade. Altitude 0.7 points per 100 m, shade 1.1 points for shaded farms; adjusted R² 0.45.

Homework 11 (25 points), due 19 November: the cup scores with a third predictor of our choice.

## Week 13. Regression diagnostics (12 November)

Plots of residuals against fitted values and against each predictor; a normal quantile plot of the residuals; points with high leverage and their influence (Cook's distance above 1 deserves a look). Transforming y with a logarithm when the spread grows with the mean.

Example: the farm with 900 kg from week 2 is a point of high leverage in the regression of deliveries on area; without it the slope drops by a third.

Homework 12 (25 points), due 26 November: diagnostics of homework 11's model.

## Final exam

Friday 11 December 2026, 18:00 to 21:00, room B-204. Covers weeks 1 to 13, 40 % of the grade. Allowed: one A4 sheet of notes, written on both sides, and a calculator. No phones.

Week 14 (3 December) is a review session; Dr. Salas will answer questions sent by email before 1 December.
