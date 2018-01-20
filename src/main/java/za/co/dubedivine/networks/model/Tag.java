package za.co.dubedivine.networks.model;

import kotlin.jvm.internal.Intrinsics;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import za.co.dubedivine.networks.model.shared.TagBase;

import java.util.Date;
import java.util.Set;

/**
 * Created by divine on 2017/08/13.
 */


//@QueryEntity what is this
@Document
public class Tag extends TagBase {

    public Tag() {
    }

    public Tag(String name) {
        super(name);
    }
}
