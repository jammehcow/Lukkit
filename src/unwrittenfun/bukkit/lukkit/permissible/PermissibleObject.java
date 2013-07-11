package unwrittenfun.bukkit.lukkit.permissible;

import org.bukkit.permissions.Permissible;

import unwrittenfun.bukkit.lukkit.LukkitObject;
import unwrittenfun.bukkit.lukkit.serveroperator.ServerOperatorObject;

public class PermissibleObject extends LukkitObject {
	public Permissible permissible;

	public PermissibleObject(Permissible p) {
		permissible = p;
		
		extendWith(new ServerOperatorObject(permissible));

		set("addAttachment", new AddAttachmentFunction()); // TODO: Return PermissionAttachment 
//		set("getEffectivePermissions", new GetEffectivePermissionsFunction());
		set("hasPermission", new HasPermissionFunction());
		set("isPermissionSet", new IsPermissionSetFunction());
		set("recalculatePermissions", new RecalculatePermissionsFunction());
//		set("removeAttachment", new RemoveAttachmentFunction());
	}
	
	@Override
	public Object getObject() {
		return permissible;
	}
}
