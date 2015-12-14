package uriage;

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

public class UriageSyuukei2 {
	public static void main(String[] args) throws IOException {

		Map<String, String> branchList = new HashMap<>(); // branchListの格納
		Map<String, Integer> branchSales = new HashMap<>(); // 店舗別売上げ合計の格納
		Map<String, String> commodityList = new HashMap<>(); // commodityListの格納
		Map<String, Integer> commoditySales = new HashMap<>(); // 商品別売上げ合計の格納

		// リストをつくる（args、読み込みファイル名、売上合計の格納先、リストの格納先、エラー表示名）
		makeLists(args, "branch.lst", 3, branchSales, branchList, "支店");
		makeLists(args, "commodity.lst", 8, commoditySales, commodityList, "商品");
		
		// ファイル名の連番確認用リスト
		try {
			List<String> uriCheckList = new ArrayList<String>(); 
			File dir = new File(args[0]);
			File path = dir;
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				List<String> uriageList = new ArrayList<>(); // 売上げリストの格納

				String fileName = files[i].getName();
				if (files[i].isFile() && fileName.endsWith(".rcd")) {

					// ファイル名の連番確認用
					int lastPosition = fileName.lastIndexOf('.'); 
					if (lastPosition == 8) {
						String checkName;
						checkName = fileName.substring(0, lastPosition);
						uriCheckList.add(checkName);
					}

					// 売上リストの取得
					BufferedReader uri = new BufferedReader(new FileReader( 
							files[i]));

					// ファイル名の連番チェック(1
					int n = 0; 
					String line = "";
					while ((line = uri.readLine()) != null) {
						++n;

						if (n == 3) {
							if (line.length() < 10) {
							} else {
								System.out.println("合計金額が10桁を超えました");
								System.exit(0);
							}
						}
						if (n < 4) {
							uriageList.add(line);
						} else {
							System.out.println(fileName + "のフォーマットが不正です");
							System.exit(0);
						}
					}
					uri.close(); // 閉じる

					// 売上の集計
					syuukei(branchSales, uriageList, 0, fileName, "店舗");
					syuukei(commoditySales, uriageList, 1, fileName, "商品");
					// 集計の出力(args、リスト、売上合計、出力ファイル名）
					output(args, branchList, branchSales, "branch.out");
					output(args, commodityList, commoditySales, "commodity.out");
				}
			}

			// ファイル名の連番チェック(2
			for (int j = 0; j < uriCheckList.size(); j++) {
				int name = Integer.parseInt(uriCheckList.get(j));
				if (name == (j + 1)) {
				} else {
					System.out.println("売上ファイル名が連番になっていません");
					System.exit(0);
				}
			}
		} catch (IOException err) {
			System.out.println("売上リスト取得時のエラー\n" + err);
		}
	}

	// リストの作成
	private static void makeLists(String[] args, String listFileName, int keta,
			Map<String, Integer> salesMap, Map<String, String> listMap,
			String errorWord) throws IOException {
		try {
			BufferedReader br = new BufferedReader(new FileReader(args[0]
					+ File.separator + listFileName));
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] bra = line.split(",", -1);
				if (bra[0].length() == keta) {
					if(bra.length == 2){
					salesMap.put(bra[0], 0);
					listMap.put(bra[0], bra[1]);
					} else {
					System.out.println(errorWord + "定義ファイルのフォーマットが不正です");
					System.exit(0);
					}
				}
			}
			br.close();
		} catch (FileNotFoundException err) {
			System.out.println(errorWord + "ファイルが存在しません");
			System.exit(0);
		} catch(ArrayIndexOutOfBoundsException err2){
			System.out.println(errorWord + "定義ファイルのフォーマットが不正です");
			System.exit(0);
		}
	}
	
	// 集計
	private static void syuukei(Map<String, Integer> salesMap,
			List<String> uriageList, int index, String fileName, String codeName) { 
		try {
			int hoge = 0;
			if (salesMap.get(uriageList.get(index)) != null) {
				hoge = salesMap.get(uriageList.get(index));
			} else {
				System.out.println(fileName + "の" + codeName + "コードが不正です");
				System.exit(0);
			}

			int hogeVlue = hoge + Integer.parseInt(uriageList.get(2));
			salesMap.put(uriageList.get(index), hogeVlue);
			String VlueStr = Integer.toString(hogeVlue);

			// 桁数チェック
			if (VlueStr.length() < 10) { 
			} else {
				System.out.println("合計金額が10桁を超えました");
				System.exit(0);
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	// ファイルのアウトプット
	private static void output(String[] args, Map<String, String> listMap,
			Map<String, Integer> salesMap, String outFileName) {
		
		List<Map.Entry<String, Integer>> ent = new ArrayList<Map.Entry<String, Integer>>(
				salesMap.entrySet());
		Collections.sort(ent, new Comparator<Entry<String, Integer>>() { // 降順にソート
					public int compare(Entry<String, Integer> o1,
							Entry<String, Integer> o2) {
						return o2.getValue().compareTo(o1.getValue());
					}
				});

		try {
			File file = new File(args[0] + File.separator + outFileName);
			PrintWriter filePw = new PrintWriter(new BufferedWriter(
					new FileWriter(file)));
			for (Entry<String, Integer> e : ent) {
				filePw.println(e.getKey() + "," + listMap.get(e.getKey()) + ","
						+ e.getValue());
			}
			filePw.close();
		} catch (IOException err) {
			System.out.println("出力時のエラー\n" + err);
		}
	}
}