# 設定

[English](config.md) | 日本語

[README](../README.ja.md) に戻る。

設定は `config/caldarium-server.toml` にあります。

## ティア

設定はティアごとに分かれていて、それぞれ別に変えられます。既定値は Tier 1 の値に下の表の倍率を
掛けたものです。2,147,483,647 を超える値は 2,147,483,647 になります。

| ティア | 1つ前のティアとの比 | Tier 1 との比 |
|---|---:|---:|
| 1 | — | ×1 |
| 2 | ×2 | ×2 |
| 3 | ×2 | ×4 |
| 4 | ×3 | ×12 |
| 5 | ×4 | ×48 |
| 6 | ×6 | ×288 |
| 7 | ×8 | ×2,304 |
| 8 | ×8 | ×18,432 |

各ブロックの既定値は [ブロック](blocks.ja.md) にあります。

## 発電機

セクション：`generator.<ティア>_<発電機>`（例：`generator.gold_crucible`）。雷変換機は `generator.bidental` です。

| 設定 | |
|---|---|
| `capacity` | 蓄電量（FE）。 |
| `transferRate` | 1 tick に接しているブロック1つずつへ送る量（FE）。 |
| `generates` / `everyTicks` | `everyTicks` tick ごとに `generates` FE を発電します。経験値発電機・生命発電機・ダメージ発電機では経験値1ポイント・体力1・1ダメージあたりの量です。雷変換機は代わりに `storm` の設定を使います。 |
| `tank` | 溶岩発電機のみ。タンクの容量（mB）。 |
| `lethalHealth` | 生命発電機のみ。倒せる最大体力の上限。 |

## 蓄電器・充電台・ケーブル

セクション：`battery.<ティア>`・`charger.<ティア>`・`cable.<ティア>`・`importer.<ティア>`・`exporter.<ティア>`。

| 設定 | |
|---|---|
| `capacity` | 蓄電量（FE）。 |
| `transferRate` | 1 tick に接しているブロック1つずつへ送る量（FE）。充電台ではアイテムを充電する速さも兼ねます。 |

## その他の設定

| 設定 | |
|---|---|
| `sun.through` | 太陽光発電機の真上の透明なブロック1つを通る日光の割合（%）。 |
| `heat.temperatures` | 熱電発電機で使うブロックの温度。`ブロック=値` または `#タグ=値` の形で書きます。 |
| `heat.span` | 熱電発電機の1組の面が最大の発電量になる温度差。 |
| `experience.pourSteps` | 経験値発電機のボタンが注ぎ込む経験値の量。「全部」のボタンは必ず付きます。 |
| `storm.natural` / `storm.summoned` | 雷変換機の雷1回あたりの発電量（自然の雷／プレイヤーが呼んだ雷）。 |
| `storm.reach` | 雷変換機から何ブロック以内の雷を受け取るか。 |
| `sol.size` | 人工太陽の直径（ブロック）。 |
| `sol.durability` | 人工太陽がもつ時間（tick）。 |
| `sol.weatherCost` | 雨や雪が当たっているときに減る速さの倍率。 |
| `sol.reach` | 表面からの範囲（ブロック）。範囲内では太陽光発電機と人工光発電機が最大の発電量になり、地面が照らされ、生き物がダメージを受けます。 |
| `sol.reachDamage` / `sol.reachEvery` | 範囲内の生き物が受けるダメージとその間隔（tick）。 |
| `sol.touchDamage` | 人工太陽に触れている間に 1 tick ごとに受けるダメージ。 |
| `sol.burnReach` / `sol.burnSeconds` / `sol.burnDamage` | 燃える範囲（表面からのブロック数）・燃える秒数・範囲内で受けるダメージ。 |
| `sol.blastReach` / `sol.blastDamage` | 壊したときの爆発の範囲（表面からのブロック数）とダメージ。 |
| `sol.hold` | 置くときに自分と人工太陽の間にあける隙間（ブロック）。 |
| `sol.glowSpacing` | 人工太陽の周りに置く光源の間隔（ブロック）。 |
| `sol.groundSpacing` | 範囲内の地面に置く光源の間隔（ブロック）。 |
| `sol.pulseEvery` / `sol.pulseLength` | 人工太陽が明るく脈打つ間隔と長さ（tick）。 |
