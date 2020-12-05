package com.denizenscript.denizencore.flags;

import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.TimeTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.utilities.Deprecations;
import com.denizenscript.denizencore.utilities.debugging.SlowWarning;
import com.denizenscript.denizencore.utilities.debugging.Warning;

import java.util.Collection;

public abstract class AbstractFlagTracker {

    public abstract ObjectTag getFlagValue(String key);

    public abstract TimeTag getFlagExpirationTime(String key);

    public abstract Collection<String> listAllFlags();

    public abstract void setFlag(String key, ObjectTag value, TimeTag expiration);

    public boolean hasFlag(String key) {
        return getFlagValue(key) != null;
    }

    public <T extends FlaggableObject> void registerFlagHandlers(ObjectTagProcessor<T> processor) {

        // <--[tag]
        // @attribute <FlaggableObject.flag[<name>]>
        // @returns ObjectTag
        // @description
        // Returns the specified flag from the flaggable object.
        // If the flag is expired, will return null.
        // Consider also using <@link tag FlaggableObject.has_flag>.
        // See <@link language Flag System>.
        // -->
        processor.registerTag("flag", (attribute, object) -> {
            return object.getFlagTracker().doFlagTag(attribute);
        });

        // <--[tag]
        // @attribute <FlaggableObject.has_flag[<flag_name>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the flaggable object has the specified flag, otherwise returns false.
        // See <@link language Flag System>.
        // -->
        processor.registerTag("has_flag", (attribute, object) -> {
            return object.getFlagTracker().doHasFlagTag(attribute);
        });

        // <--[tag]
        // @attribute <FlaggableObject.flag_expiration[<flag_name>]>
        // @returns TimeTag
        // @description
        // Returns a TimeTag indicating when the specified flag will expire.
        // See <@link language Flag System>.
        // -->
        processor.registerTag("flag_expiration", (attribute, object) -> {
            return object.getFlagTracker().doFlagExpirationTag(attribute);
        });

        // <--[tag]
        // @attribute <FlaggableObject.list_flags>
        // @returns ListTag
        // @description
        // Returns a list of the flaggable object's flags.
        // Note that this is exclusively for debug/testing reasons, and should never be used in a real script.
        // See <@link language Flag System>.
        // -->
        processor.registerTag("list_flags", (attribute, object) -> {
            return object.getFlagTracker().doListFlagsTag(attribute);
        });
    }

    public ElementTag doHasFlagTag(Attribute attribute) {
        if (!attribute.hasContext(1)) {
            attribute.echoError("The has_flag[...] tag must have an input!");
            return null;
        }
        return new ElementTag(hasFlag(attribute.getContext(1)));
    }

    public ObjectTag doFlagTag(Attribute attribute) {
        if (!attribute.hasContext(1)) {
            attribute.echoError("The flag[...] tag must have an input!");
            return null;
        }
        if (attribute.getAttributeWithoutContext(2).equals("is_expired")) {
            Deprecations.flagIsExpiredTag.warn(attribute.context);
            return new ElementTag(!hasFlag(attribute.getContext(1)));
        }
        else if (attribute.getAttributeWithoutContext(2).equals("expiration")) {
            Deprecations.flagExpirationTag.warn(attribute.context);
            TimeTag time = getFlagExpirationTime(attribute.getContext(1));
            if (time == null) {
                return null;
            }
            return new DurationTag((TimeTag.now().millis() - time.millis()) / 1000.0);
        }
        return getFlagValue(attribute.getContext(1));
    }

    public TimeTag doFlagExpirationTag(Attribute attribute) {
        if (!attribute.hasContext(1)) {
            attribute.echoError("The flag_expiration[...] tag must have an input!");
            return null;
        }
        return getFlagExpirationTime(attribute.getContext(1));
    }

    public static Warning listFlagsTagWarning = new SlowWarning("The list_flags tag is meant for testing/debugging only. Do not use it in scripts (ignore this warning if using for testing reasons).");

    public ListTag doListFlagsTag(Attribute attribute) {
        listFlagsTagWarning.warn(attribute.context);
        ListTag list = new ListTag();
        list.addAll(listAllFlags());
        return list;
    }
}
