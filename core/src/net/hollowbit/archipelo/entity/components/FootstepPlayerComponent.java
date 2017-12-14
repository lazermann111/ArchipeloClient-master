package net.hollowbit.archipelo.entity.components;

import java.util.HashSet;

import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipelo.audio.SoundCalculator;
import net.hollowbit.archipelo.entity.EntityComponent;
import net.hollowbit.archipelo.entity.LivingEntity;
import net.hollowbit.archipelo.world.Tile;
import net.hollowbit.archipeloshared.RollableEntity;
import net.hollowbit.archipeloshared.TileSoundType;

public class FootstepPlayerComponent extends EntityComponent {
	
	protected RollableEntity rollableEntity;
	protected LivingEntity livingEntity;
	protected boolean canRoll = false;
	protected HashSet<String> possibleSoundTypes;
	
	public FootstepPlayerComponent(LivingEntity entity, boolean canRoll, TileSoundType... possibleSoundTypes) {
		super(entity);
		this.livingEntity = entity;
		
		if (entity instanceof RollableEntity && canRoll) {
			rollableEntity = (RollableEntity) entity;
			this.canRoll = true;
		}
		
		this.possibleSoundTypes = new HashSet<String>();
		for (TileSoundType type : possibleSoundTypes)
			this.possibleSoundTypes.add(type.getId());
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		Vector2 tilePos = entity.getFeetTile();
		
		if (entity.getLocation().getMap() != null) {
			String tileSound = "";
			
			Tile tile = entity.getLocation().getMap().getTileTypeAtLocation((int) tilePos.x, (int) tilePos.y);
			if (tile == null)
				tileSound = "default";
			else {
				tileSound = tile.getFootstepSound();//Get tile sound name
				
				if (!possibleSoundTypes.contains(tileSound))
					tileSound = "default";
			}
			
			//Depending on state, play different footstep sounds
			if (livingEntity.isMoving()) {
				if (canRoll && rollableEntity.isRolling())
					entity.getAudioManager().setFootstepSound(entity.getEntityType().getFootstepSound() + "/" + tileSound + "-roll", 1);
				else
					entity.getAudioManager().setFootstepSound(entity.getEntityType().getFootstepSound() + "/" + tileSound + "-walk", SoundCalculator.calculatePitch(entity.getEntityType().getSpeed(), livingEntity.getSpeed()));
			} else
				entity.getAudioManager().stopFootstepSound();
		}
	}

}
