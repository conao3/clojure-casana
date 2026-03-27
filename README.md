# clojure-casana

A Clojure CLI tool for Asana. Provides an AWS CLI-style interface: `casana <resource> <action> [options]`.

## Setup

```bash
nix develop   # enter dev environment (JDK, Clojure, cljstyle, clj-kondo)
```

Initial configuration:

```bash
make ARGS="configure" run
# Asana Access Token: ****
# Default Workspace GID (optional): 123456789
```

Generate a Personal Access Token at Asana → Profile → My Settings → Apps → Manage Developer Apps.

To find your Workspace GID, open any project in Asana and check the URL:

```
https://app.asana.com/0/{workspace_gid}/{project_gid}
```

Or run `casana workspaces list` after setting your access token.

## Usage

```bash
# Workspaces
casana workspaces list

# Users
casana users list [--workspace <gid>]

# Projects
casana projects list [--workspace <gid>]
casana projects get --gid <gid>

# Sections
casana sections list --project <gid>
casana sections get --gid <gid>

# Tasks
casana tasks list [--project <gid>] [--section <gid>] [--assignee <gid|me>]
casana tasks get --gid <gid>
casana tasks create --name <name> [--project <gid>] [--section <gid>] [--notes <text>] [--due <date>] [--assignee <gid|me>]
casana tasks update --gid <gid> [--name <name>] [--notes <text>] [--due <date>] [--assignee <gid|me>] [--dependencies <gid,...>] [--field <name=value>]
casana tasks complete --gid <gid>
casana tasks move --gid <gid> --section <gid>
casana tasks delete --gid <gid>

# Comments
casana comments list --task <gid>
casana comments create --task <gid> --text <text>

# Attachments (external links, e.g. GitHub PRs)
casana attachments list --task <gid>
casana attachments create --task <gid> --url <url> [--name <name>]
casana attachments delete --gid <gid>
```

`--assignee` accepts a user GID or `me` (the authenticated user). Use `casana users list` to find user GIDs.

`--dependencies` accepts comma-separated task GIDs (e.g. `--dependencies gid1,gid2`). Pass an empty string to clear all dependencies.

`--field` accepts `name=value` to set a custom field by name (e.g. `--field "GitHub=https://..."`). Pass an empty value to clear the field (e.g. `--field "GitHub="`).

### Global options

| Option | Default | Description |
|---|---|---|
| `--output json\|table\|text` | `table` | Output format |
| `--profile <name>` | `default` | Configuration profile |
| `--workspace <gid>` | - | Workspace GID (overrides config file) |

## Development

```bash
make run ARGS="tasks list --project 123456"   # run
make test                                      # run tests (kaocha)
make check                                     # fmt + lint
make fmt                                       # format with cljstyle
make lint                                      # lint with clj-kondo
make build                                     # build uberjar (target/casana.jar)
make clean                                     # remove build artifacts
```

## Configuration

Stored at `~/.config/casana/<profile>.edn` (default: `~/.config/casana/default.edn`):

```edn
{:access-token "your-token"
 :workspace "123456789"}
```

Multiple profiles:

```bash
casana --profile work tasks list
casana --profile personal tasks list
```
