package org.mitre.emd.utility;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;

public class EmdConfigurationAdapter extends TypeAdapter {
    @Override
    public void write(JsonWriter out, Object arg1) throws IOException {
        out.beginObject();
        out.endObject();
    }

    @Override
    public EmdConfiguration read(JsonReader in) throws IOException {
        final EmdConfiguration config = new EmdConfiguration();
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            switch (name) {
                case "factors":
                    in.beginArray();
                    ArrayList<FactorsConfiguration> factors = new ArrayList<>();
                    while(!in.peek().equals(JsonToken.END_ARRAY)) {
                        in.beginObject();
                        FactorsConfiguration factor = new FactorsConfiguration();
                        while (!in.peek().equals(JsonToken.END_OBJECT)) {
                            String factorName = in.nextName();
                            switch (factorName){
                                case "class_name":
                                    factor.className = in.nextString();
                                    break;
                                case "class_package":
                                    factor.classPackage = in.nextString();
                                    break;
                                case "description":
                                    factor.description = in.nextString();
                                    break;
                                case "return_type":
                                    factor.returnType = in.nextString();
                                    break;
                                case "children_types":
                                    in.beginArray();
                                    ArrayList<String> children = new ArrayList<>();
                                    while(!in.peek().equals(JsonToken.END_ARRAY)){
                                        children.add(in.nextString());
                                    }
                                    in.endArray();
                                    factor.childrenTypes = children;
                                    break;
                                case "eval_method":
                                    factor.evalMethod = in.nextString();
                                    break;
                                default:
                                    System.err.println("Unknown configuration key of " + name);
                                    in.skipValue();
                                    break;
                            }
                        }
                        in.endObject();
                        factors.add(factor);
                    }
                    in.endArray();
                    config.factors = factors;
            }
        }
        in.endObject();
        return config;
    }
}
