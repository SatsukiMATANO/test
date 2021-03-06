package sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SalesSummary {

	private static final String BRANCH_LST = "branch.lst"; //仕様定義ファイル名（支店）
	private static final String COMMODITY_LST = "commodity.lst"; //同上（商品）

	public static void main(String[] args) {

		Map<String, String> branchMap = new HashMap<>(); // branchMapの格納
		Map<String, Long> branchSalesMap = new HashMap<>(); // 支店別売上げ合計の格納
		Map<String, String> commodityMap = new HashMap<>(); // commodityMapの格納
		Map<String, Long> commoditySalesMap = new HashMap<>(); // 商品別売上げ合計の格納

		// リストの作成（arg、読み込みファイル名、桁数チェック、売上合計の格納先、リストの格納先、エラー表示名）
		try {
			if (!makeLists(args[0], BRANCH_LST, 3, branchSalesMap, branchMap, "支店"))
				return;
			if (!makeLists(args[0], COMMODITY_LST, 8, commoditySalesMap,
					commodityMap,
					"商品"))
				return;
		} catch (Exception e) {
			return;
		}

		//売上ファイルの処理
		List<String> salesCheckList = new ArrayList<>(); // ファイル名の連番確認用リスト
		File[] files = new File(args[0]).listFiles();
		for (int i = 0; i < files.length; i++) {
			String fileName = files[i].getName();
			List<String> salesRcdList = new ArrayList<>(); // 売上げリストの格納

			//ファイルの連番のチェック
			if (files[i].isFile() && fileName.endsWith(".rcd")) {
				int lastPosition = fileName.lastIndexOf('.');
				if (lastPosition == 8) {
					String checkName = fileName.substring(0, lastPosition);
					salesCheckList.add(checkName);

					int minSalesFileName = Integer.parseInt(Collections
							.min(salesCheckList)); //rcdファイル名の最小値
					for (int j = 0; j < salesCheckList.size(); j++) {
						try {
							int name = Integer.parseInt(salesCheckList.get(j));
							if (name != minSalesFileName++) {
								System.out.println("売上ファイル名が連番になっていません");
								return;
							}
						} catch (NumberFormatException e) {
						}
					}

					//もしファイル名が8桁の数字で構成されていればリストへ格納
					//ファイルの中身が3行でない場合フォーマットエラーを表示し処理終了
					if (checkName.matches("^[0-9]+$")) {
						BufferedReader sales = null;
						try {
							sales = new BufferedReader(new FileReader(files[i]));
							int n = 0;
							String line = "";
							while ((line = sales.readLine()) != null) {
								++n;
								salesRcdList.add(line); //Rcdリストの格納コード
							}
							if (n != 3) {
								System.out.println(fileName + "のフォーマットが不正です");
								return;
							}
						} catch (Exception e) {
							System.out.println("売上ファイル読み込み時にエラーが発生しました");
						} finally {
							try {
								sales.close();
							} catch (IOException e) {
								System.out.println("売上ファイルを閉じる際にエラーが発生しました");
							}
						}

						// 売上の集計
						if (!total(branchSalesMap, salesRcdList, 0, fileName, "支店"))
							return;
						if (!total(commoditySalesMap, salesRcdList, 1, fileName,
								"商品"))
							return;
					}
				}
			}
		}

		// ファイルのアウトプットメソッド
		if (!output(args[0], branchMap, branchSalesMap, "branch.out"))
			return;
		if (!output(args[0], commodityMap, commoditySalesMap, "commodity.out"))
			return;
	}

	// リストの作成
	private static boolean makeLists(String arg, String listFileName, int keta,
			Map<String, Long> salesMap, Map<String, String> listMap,
			String errorWord) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(arg, listFileName)));
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] list = line.split(",", -1);

				if (list[0].length() != keta || list.length != 2) { //桁数、要素数のエラー
					System.out.println(errorWord + "定義ファイルのフォーマットが不正です");
					return false;
				}
				Long firstSalesValue = 0L;
				salesMap.put(list[0], firstSalesValue);
				listMap.put(list[0], list[1]);

				if (listFileName.equals(BRANCH_LST)) { //支店コードチェック（数字のみ）
					if (!list[0].matches("^[0-9]+$")) {
						System.out.println("支店定義ファイルのフォーマットが不正です");
						return false;
					}
				}

				if (listFileName.equals(COMMODITY_LST)) { //商品コードチェック（英数字のみ）
					if (!list[0].matches("^[a-zA-Z0-9]+$")) {
						System.out.println("商品定義ファイルのフォーマットが不正です");
						return false;
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println(errorWord + "ファイルが存在しません");
			return false;
		} finally {
			br.close();
		}
		return true;
	}

	// 売上の集計
	private static boolean total(Map<String, Long> salesMap,
			List<String> salesList, int index, String fileName, String codeName) {
		try {
			Long sales = 0L;
			if (salesMap.get(salesList.get(index)) == null) {
				System.out.println(fileName + "の" + codeName + "コードが不正です");
				return false;
			}
			sales = salesMap.get(salesList.get(index));
			Long salesValue = sales + Long.parseLong(salesList.get(2));
			salesMap.put(salesList.get(index), salesValue);
			String valueStr = String.valueOf(salesValue);

			// 桁数チェック
			if (valueStr.length() > 10) {
				System.out.println("合計金額が10桁を超えました");
				return false;
			}
		} catch (NumberFormatException e) {
			System.out.println(fileName + "のフォーマットが不正です");
			return false;
		}
		return true;
	}

	// ファイルのアウトプット
	private static boolean output(String arg, Map<String, String> listMap,
			Map<String, Long> salesMap, String outFileName) {

		List<Entry<String, Long>> ent = new ArrayList<>(salesMap.entrySet());
		Collections.sort(ent, new Comparator<Entry<String, Long>>() { // 降順にソート
					public int compare(Entry<String, Long> o1,
							Entry<String, Long> o2) {
						return o2.getValue().compareTo(o1.getValue());
					}
				});

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(
					arg, outFileName))));
			for (Entry<String, Long> e : ent) {
				pw.println(e.getKey() + "," + listMap.get(e.getKey()) + ","
						+ e.getValue());
			}
		} catch (IOException e) {
			System.out.println("データの出力時にエラーが発生しました");
			return false;
		} finally {
			pw.close();
		}
		return true;
	}
}