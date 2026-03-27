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

# Projects
casana projects list [--workspace <gid>]
casana projects get --gid <gid>

# Sections
casana sections list --project <gid>
casana sections get --gid <gid>

# Tasks
casana tasks list [--project <gid>] [--section <gid>] [--assignee <gid>]
casana tasks get --gid <gid>
casana tasks create --name <name> [--project <gid>] [--section <gid>] [--due <date>]
casana tasks update --gid <gid> [--name <name>] [--due <date>]
casana tasks complete --gid <gid>
casana tasks move --gid <gid> --section <gid>
casana tasks delete --gid <gid>

# Comments
casana comments list --task <gid>
casana comments create --task <gid> --text <text>
```

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
