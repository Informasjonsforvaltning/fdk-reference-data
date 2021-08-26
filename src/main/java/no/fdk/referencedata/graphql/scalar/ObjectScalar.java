package no.fdk.referencedata.graphql.scalar;

import graphql.Assert;
import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.NullValue;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.language.VariableReference;
import graphql.language.ObjectField;
import graphql.scalars.util.Kit;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ObjectScalar extends GraphQLScalarType {

    public ObjectScalar() {
        this("Object", "An object scalar");
    }

    ObjectScalar(String name, String description) {
        super(name, description, new Coercing<Object, Object>() {
            public Object serialize(Object input) throws CoercingSerializeException {
                return input;
            }

            public Object parseValue(Object input) throws CoercingParseValueException {
                return input;
            }

            public Object parseLiteral(Object input) throws CoercingParseLiteralException {
                return this.parseLiteral(input, Collections.emptyMap());
            }

            public Object parseLiteral(Object input, Map<String, Object> variables)
                    throws CoercingParseLiteralException {
                if (!(input instanceof Value)) {
                    throw new CoercingParseLiteralException("Expected AST type 'StringValue' but" +
                            " was '" + Kit.typeName(input) + "'.");
                } else if (input instanceof NullValue) {
                    return null;
                } else if (input instanceof FloatValue) {
                    return ((FloatValue)input).getValue();
                } else if (input instanceof StringValue) {
                    return ((StringValue)input).getValue();
                } else if (input instanceof IntValue) {
                    return ((IntValue)input).getValue();
                } else if (input instanceof BooleanValue) {
                    return ((BooleanValue)input).isValue();
                } else if (input instanceof EnumValue) {
                    return ((EnumValue)input).getName();
                } else if (input instanceof VariableReference) {
                    String varName = ((VariableReference)input).getName();
                    return variables.get(varName);
                } else {
                    List<?> values;
                    if (input instanceof ArrayValue) {
                        values = ((ArrayValue)input).getValues();
                        return values.stream().map((v) -> this.parseLiteral(v, variables)).collect(Collectors.toList());
                    } else if (input instanceof ObjectValue) {
                        values = ((ObjectValue)input).getObjectFields();
                        Map<String, Object> parsedValues = new LinkedHashMap<>();
                        values.forEach((fld) -> {
                            Object parsedValue = this.parseLiteral(((ObjectField)fld).getValue(),
                                    variables);
                            parsedValues.put(((ObjectField)fld).getName(), parsedValue);
                        });
                        return parsedValues;
                    } else {
                        return Assert.assertShouldNeverHappen("We have covered all Value types");
                    }
                }
            }
        });
    }
}