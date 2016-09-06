# BUILDer

A Better User Interface for Linked Data ... er

**This is work in progress!**

BUILDer is a collection of tools for working with [linked data](http://linkeddata.org) and [ontologies](https://en.wikipedia.org/wiki/Ontology_%28information_science%29). There are two main parts:

1. tasks for working with linked data
2. web-based interfaces for configuring those tasks

The tasks can be run from the web interface, the command-line, or used as libraries as part of your workflow. The web interfaces make it easier to browse, edit, and query your data, run these tasks, and check that everything is working properly.

BUILDer comes with "batteries included" defaults, but can be customized by selecting various combinations of tools, and by creating your own tasks and interfaces.


## Code Style

BUILDer tools can be used separately, or together in flexible combinations. We follow some shared conventions to make it simple and easy to use the tools in their various combinations.

1. Code is for Humans: The better we are at telling each other what we *want* the machine to do, the more likely it is that the machine will do it properly. The code says *how* to do stuff, and the comments say *why* we decided to do it that way.

2. Less Code: The less code there is, the less us humans have to understand, and the faster we can read, write, debug, and maintain it. The same goes for documentation: keep it short, simple, and to the point.

3. Consistent Style: We follow a [style guide for Clojure code](https://github.com/bbatsov/clojure-style-guide), enforced by [cljfmt](https://github.com/weavejester/cljfmt). Consistency helps us communicate clearly, keeping the focus on the important parts of the code.

