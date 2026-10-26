---
tags: [smoke, markdown-extensions]
---
# Tasks and callouts

## Tasks

- [ ] Buy corn flour
- [x] Soak the beans
- [ ] Cook for the week
  - [ ] Arepas on Monday
  - [x] Pasta e fagioli on Tuesday
1. [ ] A numbered task
2. [X] A numbered task done with a capital X

## Callouts

> [!note]
> A note without a title shows its type as the title.

> [!abstract] Abstract
> A summary of the week.

> [!info] Info
> Callouts are quotes whose first line is a type in brackets.

> [!todo] To do
> - [ ] A task inside a callout
> - [x] A finished task inside a callout

> [!tip] Tip
> Soak the beans overnight.

> [!success] Success
> The soup was ready on time.

> [!question] Question
> How long do the beans keep?

> [!warning] Warning
> Hot oil splashes.

> [!failure] Failure
> The first batch burned.

> [!danger] Danger
> Never leave the stove on.

> [!bug] Bug
> The timer did not ring.

> [!example] Example
> ```kotlin
> val servings = 6
> ```

> [!quote] Quote
> Cooking is love made visible.

## Aliases and unknown types

> [!caution] Caution is a warning
> Shown in the warning color.

> [!tldr] TL;DR is an abstract
> Shown like an abstract.

> [!RECIPE] An unknown type is a note
> Shown like a note, never as broken text.

## Folding

> [!faq]- A folded question (click to open)
> This text is hidden until the title is clicked.

> [!tip]+ An open tip (click to fold)
> This text is shown until the title is clicked.

## Nesting

> [!info] Level one
> Outer text.
> > [!warning] Level two
> > Middle text.
> > > [!danger] Level three
> > > Inner text.

> A plain quote is not a callout.
