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
	public static void main(String[] args) {

		Map<String, String> branchMap = new HashMap<>(); // branchMapの格納
		Map<String, Integer> branchSales = new HashMap<>(); // 店舗別売上げ合計の格納
		Map<String, String> commodityMap = new HashMap<>(); // commodityMapの格納
		Map<String, Integer> commoditySales = new HashMap<>(); // 商品別売上げ合計の格納

		// リストをつくる（args、読み込みファイル名、売上合計の格納先、リストの格納先、エラー表示名）
		try {
			int i = makeLists(args[0], "branch.lst", 3, branchSales, branchMap,
					"支店");
			if (i < 0){
				return;
			}
			int j = makeLists(args[0], "commodity.lst", 8, commoditySales,
					commodityMap, "商品");
			if (j < 0) {
				return;
			}
		} catch (Exception e) {
			return;
		}

		// ファイル名の連番確認用リスト
		List<String> salesCheckList = new ArrayList<>();
		File path = new File(args[0]);
		File[] files = path.listFiles();
		for (int i = 0; i < files.length; i++) {
			List<String> salesList = new ArrayList<>(); // 売上げリストの格納

			String fileName = files[i].getName();
			if (files[i].isFile() && fileName.endsWith(".rcd")) {

				// ファイル名の連番確認用
				int lastPosition = fileName.lastIndexOf('.');
				if (lastPosition == 8) {
					String checkName;
					checkName = fileName.substring(0, lastPosition);
					salesCheckList.add(checkName);
				}
				try {
					// 売上リストの取得
					BufferedReader sales = new BufferedReader(new FileReader(
							files[i]));

					// ファイル名の連番チェック(1
					int n = 0;
					String line = "";
					while ((line = sales.readLine()) != null) {
						++n;
						if (n == 3) {
							if (line.length() > 10) {
								System.out.println("合計金額が10桁を超えました"); // error
								break;
							}
						}
						if (n > 3) {
							System.out.println(fileName + "のフォーマットが不正です"); // error
							break;
						} else {
							salesList.add(line);
						}
					}
					sales.close(); // 閉じる

					// ファイル名の連番チェック(2
					for (int j = 0; j < salesCheckList.size(); j++) {
						int name = Integer.parseInt(salesCheckList.get(j));
						if (name != (j + 1)) {
							System.out.println("売上ファイル名が連番になっていません"); // error
							break;
						}
					}
				} catch (Exception e) {
					return;
				}
					// 売上の集計
					int tb = total(branchSales, salesList, 0, fileName, "店舗");
					if (tb < 0) {
						return;
					}
					int tc = total(commoditySales, salesList, 1, fileName, "商品");
					if (tc < 0) {
						return;
					}
					// 集計の出力(args、リスト、売上合計、出力ファイル名）
					output(args[0], branchMap, branchSales, "branch.out");
					output(args[0], commodityMap, commoditySales,
							"commodity.out");
			}
		}
	}

	// リストの作成
	private static int makeLists(String args, String listFileName, int keta,
			Map<String, Integer> salesMap, Map<String, String> listMap,
			String errorWord) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(args + File.separator
					+ listFileName));
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] bra = line.split(",", -1);
				if (bra[0].length() != keta) {
					System.out.println(errorWord + "定義ファイルのフォーマットが不正です"); // error
					return -1;
				} else {
					if (bra.length == 2) {
						salesMap.put(bra[0], 0);
						listMap.put(bra[0], bra[1]);
					} else {
						System.out.println(errorWord + "定義ファイルのフォーマットが不正です"); // error
						return -1;
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println(errorWord + "ファイルが存在しません"); // error
			return -1;
		} finally {
			br.close();
		}
		return 0;
	}

	// 集計
	private static int total(Map<String, Integer> salesMap,
			List<String> salesList, int index, String fileName, String codeName) {
		try {
			int sales = 0;
			if (salesMap.get(salesList.get(index)) != null) {
				sales = salesMap.get(salesList.get(index));
			} else {
				System.out.println(fileName + "の" + codeName + "コードが不正です"); // error
				return -1;
			}

			int salesVlue = sales + Integer.parseInt(salesList.get(2));
			salesMap.put(salesList.get(index), salesVlue);
			String vlueStr = Integer.toString(salesVlue);

			// 桁数チェック
			if (vlueStr.length() > 10) {
				System.out.println("合計金額が10桁を超えました"); // error
				return -1;
			}
		} catch (Exception e) {
			return -1;
		}
		return 0;

	}

	// ファイルのアウトプット
	private static void output(String args, Map<String, String> listMap,
			Map<String, Integer> salesMap, String outFileName) {

		List<Entry<String, Integer>> ent = new ArrayList<>(salesMap.entrySet());
		Collections.sort(ent, new Comparator<Entry<String, Integer>>() { // 降順にソート
					public int compare(Entry<String, Integer> o1,
							Entry<String, Integer> o2) {
						return o2.getValue().compareTo(o1.getValue());
					}
				});

		File file = new File(args, outFileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			for (Entry<String, Integer> e : ent) {
				pw.println(e.getKey() + "," + listMap.get(e.getKey()) + ","
						+ e.getValue());
			}
		} catch (IOException e) {
			System.out.println("データの出力時にエラーが発生しました"); // error
		} finally {
			pw.close();
		}
	}
}