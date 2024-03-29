# clojurescript + re-frame TodoMVC example (with ICC)

This is an example demonstrating Independently Connected Components pattern with clojurescript + re-frame.

Based on the original [Reframe TodoMVC](https://github.com/day8/re-frame/tree/master/examples/todomvc) and [React + Redux + Reselect TodoMVC Example (with ICC)](https://github.com/mr-mig/todomvc-icc) example.

The example was extended to handle several lists (to show the additional comlexity). Have a look at exercises below.

There is also [a slide deck](http://slides.com/mr-mig/microsoft-to-do-23) from Alexey Migutsky's talk on ICC at FDConf 2017 (the slides are 2D, you can go down in some sections).


## Things to notice 

1. Passing IDs through components (`main-section`) rather than whole entities
1. Binding components to specific handlers (`toggle-todo-checkbox`,
   `delete-todo-button`)
1. Specializing components and reusing a template (`todo-input-new` and
   `todo-input-edit`)
1. Domains (todo, list and filter) and linking domains (`list-todo`,
   `filter-list-todo`)
1. Render props (`todo-item`)
1. Dependent events using `add-post-event-callback` and `:dispatch` effect.

Dependent events are tricky in re-frame as they are async, so you need to prepare for intermediate states on the view layer. A potential source of errors. Maybe there is a better way to do this, like reversing the events chain, but I would like to postpone such design desicions.


## What are the benefits?

This approach provides a clean structure for components and store.  
Changing the app and moving things around are much easier than with other approaches.
 
If you want to **feel** the benefits, you can fork this repo and make these exercise changes to the app:

1. Rename the list to "Home".
1. Add a second list called "Work". It should work independently from "Home" list.
1. Make task counts work per list
1. Add a third todo list called "All" containing all tasks from both "Home" and "Work" lists.
1. When todo item is in "All" list, add a label to every todo item showing which list ("Home" or "Work") it belongs to.
1. Extract filters (All, Active, Completed) into components and remove them from "Home" and "Work" list.
1. Move a chevron (complete all tasks) to the footer of "All" list. Move completed todos counter to the header of "All" list.
1. Make "Work"/"Home" and "All" lists opaque when any todo is edited in "Home"/"Work" list (you probably need a new domain?).
1. Add drag'n'drop between "Work" and "Home" lists.
1. Add drag'n'drop sorting inside "Work" and "Home" lists.
1. Measure the performance. Try passing primitives instead of todo model in props
1. Make it possible to have 10000 items in any list
    - generate test data on app start
    - use virtualized list

The exercises are listed by increasing complexity


## Getting Started

### Project Overview

* Architecture:
[Single Page Application (SPA)](https://en.wikipedia.org/wiki/Single-page_application)
* Languages
  - Front end ([re-frame](https://github.com/day8/re-frame)): [ClojureScript](https://clojurescript.org/) (CLJS)
  - CSS compilation ([`lein-garden`](https://github.com/noprompt/lein-garden)): [Clojure](https://clojure.org/)
* Dependencies
  - UI framework: [re-frame](https://github.com/day8/re-frame)
  ([docs](https://github.com/day8/re-frame/blob/master/docs/README.md),
  [FAQs](https://github.com/day8/re-frame/blob/master/docs/FAQs/README.md)) ->
  [Reagent](https://github.com/reagent-project/reagent) ->
  [React](https://github.com/facebook/react)
  - CSS rendering: [Garden](https://github.com/noprompt/garden)
* Build tools
  - Project task & dependency management: [Leiningen](https://github.com/technomancy/leiningen)
  - CLJS compilation, REPL, & hot reload: [`shadow-cljs`](https://github.com/thheller/shadow-cljs)
  - CSS compilation: [`lein-garden`](https://github.com/noprompt/lein-garden)
  - Test framework: [cljs.test](https://clojurescript.org/tools/testing)
  - Test runner: [Karma](https://github.com/karma-runner/karma)
* Development tools
  - Debugging: [CLJS DevTools](https://github.com/binaryage/cljs-devtools),
  [`re-frame-10x`](https://github.com/day8/re-frame-10x),
  [re-frisk](https://github.com/flexsurfer/re-frisk)
  - Emacs integration: [CIDER](https://github.com/clojure-emacs/cider)
  - Linter: [clj-kondo](https://github.com/borkdude/clj-kondo)

#### Directory structure

* [`/`](/../../): project config files
* [`.clj-kondo/`](.clj-kondo/): lint config and cache files (cache files are not tracked; see
[`.gitignore`](.gitignore))
* [`dev/`](dev/): source files compiled only with the [dev](#running-the-app) profile
  - [`cljs/user.cljs`](dev/cljs/user.cljs): symbols for use during development in the
[ClojureScript REPL](#connecting-to-the-browser-repl-from-a-terminal)
* [`resources/public/`](resources/public/): SPA root directory;
[dev](#running-the-app) / [prod](#production) profile depends on the most recent build
  - [`index.html`](resources/public/index.html): SPA home page
    - Dynamic SPA content rendered in the following `div`:
        ```html
        <div id="app"></div>
        ```
    - Customizable; add headers, footers, links to other scripts and styles, etc.
  - Generated directories and files
    - Created on build with either the [dev](#running-the-app) or [prod](#production) profile
    - Deleted on `lein clean` (run by all `lein` aliases before building)
    - `css/`: compiled CSS (`lein-garden`, can also be
[compiled manually](#compiling-css-with-lein-garden))
    - `js/compiled/`: compiled CLJS (`shadow-cljs`)
      - Not tracked in source control; see [`.gitignore`](.gitignore)
* [`src/clj/todomvc_icc_cljs/`](src/clj/todomvc_icc_cljs/): CSS compilation source files (Clojure,
[Garden](https://github.com/noprompt/garden))
* [`src/cljs/todomvc_icc_cljs/`](src/cljs/todomvc_icc_cljs/): SPA source files (ClojureScript,
[re-frame](https://github.com/Day8/re-frame))
  - [`core.cljs`](src/cljs/todomvc_icc_cljs/core.cljs): contains the SPA entry point, `init`
* [`test/cljs/todomvc_icc_cljs/`](test/cljs/todomvc_icc_cljs/): test files (ClojureScript,
[cljs.test](https://clojurescript.org/tools/testing))
  - Only namespaces ending in `-test` (files `*_test.cljs`) are compiled and sent to the test runner

### Editor/IDE

Use your preferred editor or IDE that supports Clojure/ClojureScript development. See
[Clojure tools](https://clojure.org/community/resources#_clojure_tools) for some popular options.

### Environment Setup

1. Install [JDK 8 or later](https://openjdk.java.net/install/) (Java Development Kit)
2. Install [Leiningen](https://leiningen.org/#install) (Clojure/ClojureScript project task &
dependency management)
3. Install [Node.js](https://nodejs.org/) (JavaScript runtime environment) which should include
   [NPM](https://docs.npmjs.com/cli/npm) or if your Node.js installation does not include NPM also install it.
4. Install [karma-cli](https://www.npmjs.com/package/karma-cli) (test runner):
    ```sh
    npm install -g karma-cli
    ```
5. Install [Chrome](https://www.google.com/chrome/) or
[Chromium](https://www.chromium.org/getting-involved/download-chromium) version 59 or later
(headless test environment)
    * For Chromium, set the `CHROME_BIN` environment variable in your shell to the command that
    launches Chromium. For example, in Ubuntu, add the following line to your `.bashrc`:
        ```bash
        export CHROME_BIN=chromium-browser
       ```
6. Install [clj-kondo](https://github.com/borkdude/clj-kondo/blob/master/doc/install.md) (linter)
7. Clone this repo and open a terminal in the `todomvc-icc-cljs` project root directory
8. (Optional) Download project dependencies:
    ```sh
    lein deps
    ```
9. (Optional) Setup [lint cache](https://github.com/borkdude/clj-kondo#project-setup):
    ```sh
    clj-kondo --lint "$(lein classpath)"
    ```
10. Setup
[linting in your editor](https://github.com/borkdude/clj-kondo/blob/master/doc/editor-integration.md)

### Browser Setup

Browser caching should be disabled when developer tools are open to prevent interference with
[`shadow-cljs`](https://github.com/thheller/shadow-cljs) hot reloading.

Custom formatters must be enabled in the browser before
[CLJS DevTools](https://github.com/binaryage/cljs-devtools) can display ClojureScript data in the
console in a more readable way.

#### Chrome/Chromium

1. Open [DevTools](https://developers.google.com/web/tools/chrome-devtools/) (Linux/Windows: `F12`
or `Ctrl-Shift-I`; macOS: `⌘-Option-I`)
2. Open DevTools Settings (Linux/Windows: `?` or `F1`; macOS: `?` or `Fn+F1`)
3. Select `Preferences` in the navigation menu on the left, if it is not already selected
4. Under the `Network` heading, enable the `Disable cache (while DevTools is open)` option
5. Under the `Console` heading, enable the `Enable custom formatters` option

#### Firefox

1. Open [Developer Tools](https://developer.mozilla.org/en-US/docs/Tools) (Linux/Windows: `F12` or
`Ctrl-Shift-I`; macOS: `⌘-Option-I`)
2. Open [Developer Tools Settings](https://developer.mozilla.org/en-US/docs/Tools/Settings)
(Linux/macOS/Windows: `F1`)
3. Under the `Advanced settings` heading, enable the `Disable HTTP Cache (when toolbox is open)`
option

Unfortunately, Firefox does not yet support custom formatters in their devtools. For updates, follow
the enhancement request in their bug tracker:
[1262914 - Add support for Custom Formatters in devtools](https://bugzilla.mozilla.org/show_bug.cgi?id=1262914).

## Development

### Running the App

Start a temporary local web server, build the app with the `dev` profile, and serve the app,
browser test runner and karma test runner with hot reload:

```sh
lein watch
```

Please be patient; it may take over 20 seconds to see any output, and over 40 seconds to complete.

When `[:app] Build completed` appears in the output, browse to
[http://localhost:8280/](http://localhost:8280/).

[`shadow-cljs`](https://github.com/thheller/shadow-cljs) will automatically push ClojureScript code
changes to your browser on save. To prevent a few common issues, see
[Hot Reload in ClojureScript: Things to avoid](https://code.thheller.com/blog/shadow-cljs/2019/08/25/hot-reload-in-clojurescript.html#things-to-avoid).

Opening the app in your browser starts a
[ClojureScript browser REPL](https://clojurescript.org/reference/repl#using-the-browser-as-an-evaluation-environment),
to which you may now connect.

#### Connecting to the browser REPL from Emacs with CIDER

Connect to the browser REPL:
```
M-x cider-jack-in-cljs
```

See
[Shadow CLJS User's Guide: Emacs/CIDER](https://shadow-cljs.github.io/docs/UsersGuide.html#cider)
for more information. Note that the mentioned [`.dir-locals.el`](.dir-locals.el) file has already
been created for you.

#### Connecting to the browser REPL from other editors

See
[Shadow CLJS User's Guide: Editor Integration](https://shadow-cljs.github.io/docs/UsersGuide.html#_editor_integration).
Note that `lein watch` runs `shadow-cljs watch` for you, and that this project's running build ids is
`app`, `browser-test`, `karma-test`, or the keywords `:app`, `:browser-test`, `:karma-test` in a Clojure context.

Alternatively, search the web for info on connecting to a `shadow-cljs` ClojureScript browser REPL
from your editor and configuration.

For example, in Vim / Neovim with `fireplace.vim`
1. Open a `.cljs` file in the project to activate `fireplace.vim`
2. In normal mode, execute the `Piggieback` command with this project's running build id, `:app`:
    ```vim
    :Piggieback :app
    ```

#### Connecting to the browser REPL from a terminal

1. Connect to the `shadow-cljs` nREPL:
    ```sh
    lein repl :connect localhost:8777
    ```
    The REPL prompt, `shadow.user=>`, indicates that is a Clojure REPL, not ClojureScript.

2. In the REPL, switch the session to this project's running build id, `:app`:
    ```clj
    (shadow.cljs.devtools.api/nrepl-select :app)
    ```
    The REPL prompt changes to `cljs.user=>`, indicating that this is now a ClojureScript REPL.
3. See [`user.cljs`](dev/cljs/user.cljs) for symbols that are immediately accessible in the REPL
without needing to `require`.

### Running Tests

Build the app with the `prod` profile, start a temporary local web server, launch headless
Chrome/Chromium, run tests, and stop the web server:

```sh
lein ci
```

Please be patient; it may take over 15 seconds to see any output, and over 25 seconds to complete.

Or, for auto-reload:
```sh
lein watch
```

Then in another terminal:
```sh
karma start
```

### Compiling CSS with `lein-garden`

Use Clojure and [Garden](https://github.com/noprompt/garden) to edit styles in `.clj` files located
in the [`src/clj/todomvc_icc_cljs/`](src/clj/todomvc_icc_cljs/) directory. CSS files are compiled
automatically on [`dev`](#running-the-app) or [`prod`](#production) build.

Manually compile CSS files:
```sh
lein garden once
```

The `resources/public/css/` directory is created, containing the compiled CSS files.

#### Compiling CSS with Garden on change

Enable automatic compiling of CSS files when source `.clj` files are changed:
```sh
lein garden auto
```

### Running `shadow-cljs` Actions

See a list of [`shadow-cljs CLI`](https://shadow-cljs.github.io/docs/UsersGuide.html#_command_line)
actions:
```sh
lein run -m shadow.cljs.devtools.cli --help
```

Please be patient; it may take over 10 seconds to see any output. Also note that some actions shown
may not actually be supported, outputting "Unknown action." when run.

Run a shadow-cljs action on this project's build id (without the colon, just `app`):
```sh
lein run -m shadow.cljs.devtools.cli <action> app
```
### Debug Logging

The `debug?` variable in [`config.cljs`](src/cljs/todomvc_icc_cljs/config.cljs) defaults to `true` in
[`dev`](#running-the-app) builds, and `false` in [`prod`](#production) builds.

Use `debug?` for logging or other tasks that should run only on `dev` builds:

```clj
(ns todomvc-icc-cljs.example
  (:require [todomvc-icc-cljs.config :as config])

(when config/debug?
  (println "This message will appear in the browser console only on dev builds."))
```

## Production

Build the app with the `prod` profile:

```sh
lein release
```

Please be patient; it may take over 15 seconds to see any output, and over 30 seconds to complete.

The `resources/public/js/compiled` directory is created, containing the compiled `app.js` and
`manifest.edn` files.

The [`resources/public`](resources/public/) directory contains the complete, production web front
end of your app.

Always inspect the `resources/public/js/compiled` directory prior to deploying the app. Running any
`lein` alias in this project after `lein watch` will, at the very least, run `lein clean`, which
deletes this generated directory. Further, running `lein watch` will generate many, much larger
development versions of the files in this directory.
