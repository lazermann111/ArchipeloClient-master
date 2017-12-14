package net.hollowbit.archipelo.screen.screens.characterpicker;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.hollowbit.archipelo.ArchipeloClient;
import net.hollowbit.archipelo.form.MobileCompatibleWindow;
import net.hollowbit.archipelo.network.packets.PlayerListPacket;
import net.hollowbit.archipelo.screen.screens.CharacterCreatorScreen;
import net.hollowbit.archipelo.tools.LM;

public class CharacterPickWindow extends MobileCompatibleWindow {
	
	Table characterTable;
	ScrollPane characterScrollPane;
	TextButton createNewButton;
	
	public CharacterPickWindow (Stage stage) {
		super("Pick Character", ArchipeloClient.getGame().getUiSkin());
		this.setStage(stage);
		
		setMovable(false);
		
		characterTable = new Table();
		characterScrollPane = new ScrollPane(characterTable, getSkin());
		characterScrollPane.setFadeScrollBars(false);
		add(characterScrollPane).width(600).height(480);
		pack();
	}
	
	/**
	 * Reloads character list.
	 * @param playerListPacket
	 */
	public void reloadList (PlayerListPacket playerListPacket) {
		characterTable.clear();
		pack();
		
		//Add character profiles to table
		for (int i = 0; i < playerListPacket.names.length; i++)
			characterTable.add(new CharacterProfile(getStage(), this, playerListPacket.names[i], playerListPacket.playerEquippedInventories[i], playerListPacket.islands[i], playerListPacket.lastPlayedDateTimes[i], playerListPacket.creationDateTimes[i], 0)).pad(25);//Add proper levels after
		
		//If user has another character slot available, add button to create a new one
		if (playerListPacket.names.length < ArchipeloClient.MAX_CHARACTERS_PER_PLAYER)
			addCreateNewButton();
		
		//Make scrollpane height match he height of the profiles inside
		getCell(characterScrollPane).height(characterTable.getHeight() + 25);
		pack();
	}
	
	/**
	 * Adds a create character button if a character is remove and there isn't one already
	 */
	public void updateAfterCharacterRemoved () {
		if (createNewButton == null)
			addCreateNewButton();
	}
	
	private void addCreateNewButton () {
		createNewButton = new TextButton(LM.ui("createNew"), ArchipeloClient.getGame().getUiSkin());
		createNewButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				//If button is clicked, open the player creator screen
				ArchipeloClient.getGame().getScreenManager().setScreen(new CharacterCreatorScreen());
				super.clicked(event, x, y);
			}
		});
		characterTable.add(createNewButton).pad(25);
	}

}
