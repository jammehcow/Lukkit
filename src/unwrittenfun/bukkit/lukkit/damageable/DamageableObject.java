package unwrittenfun.bukkit.lukkit.damageable;

import org.bukkit.entity.Damageable;

import unwrittenfun.bukkit.lukkit.LukkitObject;
import unwrittenfun.bukkit.lukkit.entity.EntityObject;

public class DamageableObject extends LukkitObject {
	public Damageable damageable;

	public DamageableObject(Damageable d) {
		damageable = d;
		
		extendWith(new EntityObject(damageable));
		
		set("damage", new DamageFunction());
		set("getHealth", new GetHealthFunction());
		set("getMaxHealth", new GetMaxHealthFunction());
		set("resetMaxHealth", new ResetMaxHealthFunction());
		set("setHealth", new SetHealthFunction());
		set("setMaxHealth", new SetMaxHealthFunction());
	}
	
	@Override
	public Object getObject() {
		return damageable;
	}
}