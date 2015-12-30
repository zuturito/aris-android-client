package edu.uoregon.casls.aris_android.models;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.uoregon.casls.aris_android.GamePlayActivity;
import edu.uoregon.casls.aris_android.Utilities.AppUtils;
import edu.uoregon.casls.aris_android.data_objects.Game;

/**
 * Created by smorison on 12/9/15.
 */
public class GamesModel {
	public Map<Long, Game> games          = new LinkedHashMap<>();
	public Map<Long, Game> downloadedGames = new LinkedHashMap<>();
	public Date downloadedStamp;

	public transient GamePlayActivity mGamePlayAct;
//	public           Game            playerGame;

	// todo: populate methods as needed instead of using all the stock iOS ones.

	public void initContext(GamePlayActivity gamePlayAct) {
		mGamePlayAct = gamePlayAct; // todo: may need leak checking is activity gets recreated.
	}

	public Game updateGame(Game g) {
		Game existingG = this.gameForId(g.game_id);
		if (existingG != null)
			existingG.mergeDataFromGame(g);
		else
			this.games.put(g.game_id, g);
		// this dispatch may be unnecessary for the Android version
		mGamePlayAct.mDispatch.model_game_available(this.gameForId(g.game_id)); //_ARIS_NOTIF_SEND_(@"MODEL_GAME_AVAILABLE",nil,@{@"game":[self gameForId:g.game_id]});

		return this.gameForId(g.game_id);
	}

	public Game gameForId(long game_id) {
		return this.games.get(game_id);
	}

	public Map<Long, Game> pingDownloadedGames() {
		if (downloadedStamp == null || AppUtils.getTimeDiff(downloadedStamp, new Date(), TimeUnit.SECONDS) > 10) {
			downloadedStamp = new Date();
			//gen downloaded games

			List<Game> d_games = new ArrayList<>(); //NSMutableArray *d_games = [[NSMutableArray alloc] init];
			//SBJsonParser *jsonParser = [[SBJsonParser alloc] init];
			File appDir = new File(mGamePlayAct.getFilesDir().getPath());
//			NSString *rootString = [_MODEL_ applicationDocumentsDirectory];

			// get contents of directory
			File[] directoryContent = appDir.listFiles();
//			NSArray *directoryContent = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:rootString error:NULL];
			Gson gson = new Gson();
			// loop through all game files in the directory
			for (File gameFile : directoryContent) {
				if ( gameFile.isFile() && gameFile.getName().endsWith("_game.json") ) {
					String jsonStoredGame = AppUtils.readFromFileStream(mGamePlayAct, gameFile); // read raw json from stored game file
					Game g = gson.fromJson(jsonStoredGame, Game.class); // deserialize json into Game
					d_games.add(g);
				}
			}
			// send directly to updateDownloadedGames() (Android)
			if (d_games.size() > 0) this.updateDownloadedGames(d_games); // _ARIS_NOTIF_SEND_(@"SERVICES_DOWNLOADED_GAMES_RECEIVED", nil, @{@"games":d_games});
			//emulate 'services' for consistency's sake (iOS only)
//			if (d_games.size() > 0) mGamePlayAct.mDispatch.services_downloaded_games_received(d_games); // _ARIS_NOTIF_SEND_(@"SERVICES_DOWNLOADED_GAMES_RECEIVED", nil, @{@"games":d_games});
		}
		mGamePlayAct.performSelector.postDelayed(new Runnable() {
			@Override
			public void run() { notifDownloadedGames(); }
		},1000); // delay 1000ms = 1sec
//		else [self performSelector:@selector(notifDownloadedGames) withObject:nil afterDelay:1];

		return downloadedGames;
	}

	public void notifDownloadedGames() {
		mGamePlayAct.mDispatch.model_downloaded_games_available();
	}

	public void updateDownloadedGames(List<Game> d_games) {
		this.updateGames(d_games);
	}

	public List<Game> updateGames(List<Game> d_games) {
//		NSMutableArray *mergedNewGames = [[NSMutableArray alloc] initWithCapacity:newGames.count];
		List<Game> mergedNewGames = new ArrayList<>();
		for (Game newGame : d_games) {
			mergedNewGames.add(this.updateGame(newGame));
		}
		return mergedNewGames;
	}

}
