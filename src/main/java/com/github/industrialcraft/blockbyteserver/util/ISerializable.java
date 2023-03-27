package com.github.industrialcraft.blockbyteserver.util;

import java.io.DataOutputStream;
import java.io.IOException;

public interface ISerializable {
    void serialize(DataOutputStream stream) throws IOException;
}
