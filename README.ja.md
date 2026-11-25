# Caldarium

[English](README.md) | 日本語

Forge Energy（FE）の発電機・蓄電器・充電台・ケーブルを追加します。Fabric では Team Reborn Energy
で動きます。

## ブロック

| ブロック | |
|---|---|
| 火力発電機 | かまどで燃えるもので発電します |
| 溶岩発電機 | 溶岩などの液体燃料で発電します |
| 太陽光発電機 | 日光で発電します |
| 人工光発電機 | 光（日光またはブロック光）で発電します |
| 熱電発電機 | 向かい合う面の温度差で発電します |
| 経験値発電機 | 注ぎ込んだ経験値で発電します |
| 生命発電機 | 上に乗った生き物の体力で発電します |
| ダメージ発電機 | 叩かれると発電します |
| 雷変換機 | 雷で発電します |
| 蓄電器 | 蓄電します |
| 充電台 | アイテムを充電します |
| ケーブル・インポーター・エクスポーター | 送電します |
| 人工太陽 ソル | 周りの太陽光発電機と人工光発電機を動かす人工の太陽です |

発電機は Tier 1〜8、蓄電器・充電台・ケーブルは Tier 1〜6 です。雷変換機と人工太陽 ソルには
ティアがありません。

## ドキュメント

| | |
|---|---|
| [ブロック](docs/blocks.ja.md) | 各ブロックの働きとティアごとの値 |
| [エネルギーの動き方](docs/energy.ja.md) | ブロック間の送電とケーブルの接続 |
| [設定](docs/config.ja.md) | 全ての設定項目 |

## 対象

| Minecraft | ローダー | ほかに必要なもの | Java |
|---|---|---|---|
| 1.21.1 | NeoForge 21.1 | | 21 |
| 1.21.1 | Fabric Loader 0.16 以降 | Fabric API 0.116 以降 | 21 |
| 1.20.1 | Forge 47 以降 | | 17 |
| 1.20.1 | Fabric Loader 0.15 以降 | Fabric API 0.92 以降 | 17 |
| 1.19.2 | Forge 43 以降 | | 17 |
| 1.19.2 | Fabric Loader 0.14 以降 | Fabric API 0.77 以降 | 17 |
| 1.18.2 | Forge 40 以降 | | 17 |
| 1.18.2 | Fabric Loader 0.14 以降 | Fabric API 0.77 以降 | 17 |
| 1.16.5 | Forge 36 以降 | | 8 |
| 1.16.5 | Fabric Loader 0.11 以降 | Fabric API 0.42 以降 | 8 |

組み合わせごとに jar が分かれていて、名前は `caldarium-<ローダー>-<Minecraft>-<バージョン>.jar` です。

Fabric には Forge Energy がありません。Fabric では、Tech Reborn・Modern Industrialization・
Applied Energistics 2 と同じ電力の仕組みである Team Reborn Energy で動きます。jar に入っているので、
ほかに入れるものはありません。この資料の 1 FE は、Team Reborn Energy の 1 にあたります。

Fabric では、ブロックを持って太陽のほうを右クリックしても何も起きません。Forge と NeoForge では、
クリックしたブロックを使う操作はそのまま行えます。

### Minecraft 1.16.5

- この版にはまだ銅がありません。銅のティアは、ほかの Mod が追加する銅インゴット（Forge では
  `forge:ingots/copper`、Fabric では `c:copper_ingots` のタグ）から作ります。そうした Mod が無いと、
  銅のティアと、その上のティアはすべて作れません。
- Bidental は、避雷針の代わりに鉄格子を使います。
- どのブロックも、ティアにかかわらず、どのつるはしでも壊せます。
- アイテムの説明に出る数値は、表ではなく文字の行で出ます。
- Fabric では、ホッパーから燃焼式発電機に燃料を入れられません。液体の発電機に液体を入れるのはバケツだけです。

## ビルド

```
run.bat                         # コンパイルして、選択中のノードの開発クライアントを起動
gradlew build                   # すべてのノードの jar を作る
gradlew runGameTestServer       # ゲームテストのあるすべてのノードでゲームテストを実行
gradlew runData                 # テクスチャ・モデル・レシピ・言語ファイルを作り直す
python tools/autopilot.py <ノード> --world <ワールド>   # 開発クライアントで確認を自動で行って終了
```

Minecraft のバージョンとローダーの組み合わせごとに、[Stonecutter](https://stonecutter.kikugie.dev/)
のノードが1つあります。ノードの一覧は `settings.gradle.kts` にあり、それぞれ `versions/` の下に
フォルダを持ちます。jar は `versions/<ノード>/build/libs/` にできます。選択中のノードは
`stonecutter.gradle.kts` に書いてあり、`run.bat` が起動するのも、ソースに表示されるのもこのノードです。

autopilot は `src/autopilot` にある開発専用の Mod で、`runAutopilot` タスクだけが読み込み、jar には入りません。
指定したワールドを開いて、レシピ・画面・太陽とその届く距離を確かめます。Fabric では、Team Reborn Energy
の入れ物に電気が届くことも確かめます。結果を `versions/<ノード>/autopilot-run/autopilot/result.txt` に
PASS/FAIL の行で書き、目で見るしかない部分はスクリーンショットに撮ります。

Minecraft 1.16.5 にはゲームテストの仕組みが無いので、そのノードは autopilot でだけ確かめます。
1.16.5 の Forge の開発クライアントは `versions/1.16.5-forge/devfix/` も読み込みます。Architectury Loom が
名前を変換し損ねる `BlockMath` の2か所の呼び出しを直すコアモッドで、jar には入りません。

`runData` は 1.21.1 の NeoForge のノードでだけ動きます。他のノードは、ビルドのときにその出力を
自分の形式に変換します。

`JAVA_HOME` が JDK 21 を指しているか `java` が `PATH` にある必要があります。古いノードが使う JDK は
Gradle が取ってきます。

## ライセンス

MIT です。詳しくは [LICENSE](LICENSE) を見てください。
