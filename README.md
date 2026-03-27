# clojure-casana

Clojure 製の Asana CLI ツール。AWS CLI スタイルの `casana <resource> <action> [options]` インターフェースで Asana を操作できます。

## セットアップ

```bash
nix develop   # 開発環境に入る（JDK・Clojure・cljstyle・clj-kondo が使えるようになる）
```

初回設定:

```bash
make ARGS="configure" run
# Asana Access Token: ****
# Default Workspace GID (optional): 123456789
```

Personal Access Token は Asana → プロフィール → My Settings → Apps → Manage Developer Apps から発行してください。

## 使い方

```bash
# ワークスペース
casana workspaces list

# プロジェクト
casana projects list [--workspace <gid>]
casana projects get <gid>

# セクション
casana sections list --project <gid>
casana sections get <gid>

# タスク
casana tasks list [--project <gid>] [--section <gid>] [--assignee <gid>]
casana tasks get <gid>
casana tasks create --name <name> [--project <gid>] [--section <gid>] [--due <date>]
casana tasks update <gid> [--name <name>] [--due <date>]
casana tasks complete <gid>
casana tasks move <gid> --section <gid>
casana tasks delete <gid>

# コメント
casana comments list --task <gid>
casana comments create --task <gid> --text <text>
```

### グローバルオプション

| オプション | デフォルト | 説明 |
|---|---|---|
| `--output json\|table\|text` | `table` | 出力形式 |
| `--profile <name>` | `default` | 設定プロファイル |
| `--workspace <gid>` | - | ワークスペース GID（設定ファイルを上書き） |

## 開発

```bash
make run ARGS="tasks list --project 123456"   # 実行
make test                                      # テスト（kaocha）
make check                                     # fmt + lint
make fmt                                       # cljstyle でフォーマット
make lint                                      # clj-kondo でリント
make build                                     # uberjar ビルド（target/casana.jar）
make clean                                     # ビルド成果物を削除
```

## 設定ファイル

`~/.config/casana/<profile>.edn` に保存されます。

```edn
{:access-token "your-token"
 :workspace "123456789"}
```

複数プロファイルの切り替え:

```bash
casana --profile work tasks list
casana --profile personal tasks list
```
