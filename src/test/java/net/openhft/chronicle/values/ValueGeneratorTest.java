/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.values;

import net.openhft.chronicle.bytes.Byteable;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Date;

import static net.openhft.chronicle.values.Generators.generateNativeClass;
import static net.openhft.chronicle.values.Values.newHeapInstance;
import static net.openhft.chronicle.values.Values.newNativeReference;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: peter.lawrey Date: 06/10/13 Time: 20:13
 */
public class ValueGeneratorTest {
    @Test
    public void testGenerateJavaCode() {
//        JavaBeanInterface jbi = Values.newHeapInstance(JavaBeanInterface.class);
//        jbi.setByte((byte) 1);
//        jbi.setChar('2');
//        jbi.setShort((short) 3);
//        jbi.setInt(4);
//        jbi.setFloat(5);
//        jbi.setLong(6);
//        jbi.setDouble(7);
//        jbi.setFlag(true);
//
//        assertEquals(1, jbi.getByte());
//        assertEquals('2', jbi.getChar());
//        assertEquals(3, jbi.getShort());
//        assertEquals(4, jbi.getInt());
//        assertEquals(5.0, jbi.getFloat(), 0);
//        assertEquals(6, jbi.getLong());
//        assertEquals(7.0, jbi.getDouble(), 0.0);
//        assertTrue(jbi.getFlag());
    }

    @Test
    public void testGenerateJavaCode2() {
        MinimalInterface mi = newHeapInstance(MinimalInterface.class);

        mi.byte$((byte) 1);
        mi.char$('2');
        mi.short$((short) 3);
        mi.int$(4);
        mi.float$(5);
        mi.long$(6);
        mi.double$(7);
        mi.flag(true);

        assertEquals(1, mi.byte$());
        assertEquals('2', mi.char$());
        assertEquals(3, mi.short$());
        assertEquals(4, mi.int$());
        assertEquals(5.0, mi.float$(), 0);
        assertEquals(6, mi.long$());
        assertEquals(7.0, mi.double$(), 0.0);
        assertTrue(mi.flag());

        Bytes bbb = Bytes.wrapForWrite(ByteBuffer.allocate(64));
        mi.writeMarshallable(bbb);
        System.out.println("size: " + bbb.writePosition());

        MinimalInterface mi2 = newHeapInstance(MinimalInterface.class);
        bbb.readPosition(0);
        mi2.readMarshallable(bbb);

        assertEquals(1, mi2.byte$());
        assertEquals('2', mi2.char$());
        assertEquals(3, mi2.short$());
        assertEquals(4, mi2.int$());
        assertEquals(5.0, mi2.float$(), 0);
        assertEquals(6, mi2.long$());
        assertEquals(7.0, mi2.double$(), 0.0);
        assertTrue(mi2.flag());
    }

    @Test
    public void testGenerateNativeWithGetUsing() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String actual = generateNativeClass(ValueModel.acquire(JavaBeanInterfaceGetUsing.class),
                ValueModel.simpleName(JavaBeanInterfaceGetUsing.class) + "$$Native");
        System.out.println(actual);
        CachedCompiler cc = new CachedCompiler();
        Class aClass = cc.loadFromJava(JavaBeanInterfaceGetUsing.class,
                BytecodeGen.getClassLoader(JavaBeanInterfaceGetUsing.class),
                JavaBeanInterfaceGetUsing.class.getName() + "$$Native", actual);
        JavaBeanInterfaceGetUsing jbi = (JavaBeanInterfaceGetUsing) aClass.asSubclass(JavaBeanInterfaceGetUsing.class).newInstance();
        BytesStore bytes = BytesStore.wrap(ByteBuffer.allocate(64));
        ((Byteable) jbi).bytesStore(bytes, 0L, ((Byteable) jbi).maxSize());

        jbi.setString("G'day");

        assertEquals("G'day", jbi.getUsingString(new StringBuilder()).toString());
    }

    @Test
    public void testGenerateNativeWithHasArrays() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        HasArraysInterface hai = Values.newNativeReference(HasArraysInterface.class);
        BytesStore bytes = BytesStore.wrap(ByteBuffer.allocate(152));
        ((Byteable) hai).bytesStore(bytes, 0L, ((Byteable) hai).maxSize());

        hai.setStringAt(0, "G'day");

        assertEquals("G'day", hai.getStringAt(0));
    }

    @Test
    public void testGenerateNativeWithGetUsingHeapInstance() {
        JavaBeanInterfaceGetUsingHeap si = newHeapInstance(JavaBeanInterfaceGetUsingHeap.class);

        si.setString("G'day");

        assertEquals("G'day", si.getUsingString(new StringBuilder()).toString());
    }

    @Test
    public void testStringFields() {
        StringInterface si = newHeapInstance(StringInterface.class);
        si.setString("Hello world");
        assertEquals("Hello world", si.getString());

        StringInterface si2 = newNativeReference(StringInterface.class);
        BytesStore bytes = BytesStore.wrap(ByteBuffer.allocate(192));
        ((Byteable) si2).bytesStore(bytes, 0L, ((Byteable) si2).maxSize());
        si2.setString("Hello world £€");
        si2.setText("Hello world £€");
        assertEquals("Hello world £€", si2.getString());
        assertEquals("Hello world £€", si2.getText());
    }

    @Test
    public void testGetUsingStringFieldsWithStringBuilderHeapInstance() {
        GetUsingStringInterface si = newHeapInstance(GetUsingStringInterface.class);
        si.setSomeStringField("Hello world");
        si.setAnotherStringField("Hello world 2");
        assertEquals("Hello world", si.getSomeStringField());
        {
            StringBuilder builder = new StringBuilder();
            si.getUsingSomeStringField(builder);
            assertEquals("Hello world", builder.toString());
        }
        {
            StringBuilder builder = new StringBuilder();
            si.getUsingAnotherStringField(builder);
            assertEquals("Hello world 2", builder.toString());
        }
    }

    @Test
    public void testNested() {
        NestedB nestedB1 = newHeapInstance(NestedB.class);
        nestedB1.ask(100);
        nestedB1.bid(100);
        NestedB nestedB2 = newHeapInstance(NestedB.class);
        nestedB2.ask(91);
        nestedB2.bid(92);

        NestedA nestedA = newNativeReference(NestedA.class);
        BytesStore bytes = BytesStore.wrap(ByteBuffer.allocate(192));
        ((Byteable) nestedA).bytesStore(bytes, 0L, ((Byteable) nestedA).maxSize());
        nestedA.key("key");
        nestedA.one(nestedB1);
        nestedA.two(nestedB2);
        assertEquals("key", nestedA.key());
        assertEquals(nestedB1.ask(), nestedA.one().ask(), 0.0);
        assertEquals(nestedB1.bid(), nestedA.one().bid(), 0.0);
        assertEquals(nestedB2.ask(), nestedA.two().ask(), 0.0);
        assertEquals(nestedB2.bid(), nestedA.two().bid(), 0.0);
        assertEquals(nestedB1, nestedA.one());
        assertEquals(nestedB2, nestedA.two());
        assertEquals(nestedB1.hashCode(), nestedA.one().hashCode());
        assertEquals(nestedB2.hashCode(), nestedA.two().hashCode());
    }

    @Test
    public void testGenerateInterfaceWithEnumOnHeap() {
        JavaBeanInterfaceGetMyEnum jbie = newHeapInstance(JavaBeanInterfaceGetMyEnum.class);
        jbie.setMyEnum(MyEnum.B);
    }

    @Test
    public void testGenerateInterfaceWithEnumNativeInstance() {
        JavaBeanInterfaceGetMyEnum jbie = newNativeReference(JavaBeanInterfaceGetMyEnum.class);
        BytesStore bytes = BytesStore.wrap(ByteBuffer.allocate(64));
        ((Byteable) jbie).bytesStore(bytes, 0L, ((Byteable) jbie).maxSize());
        jbie.setMyEnum(MyEnum.C);
    }

    @Test
    public void testGenerateInterfaceWithDateOnHeap() {
        //dvg.setDumpCode(true);
        JavaBeanInterfaceGetDate jbid = newHeapInstance(JavaBeanInterfaceGetDate.class);
        jbid.setDate(new Date());
    }

    @Test
    public void testGenerateInterfaceWithDateNativeInstace() {
        //dvg.setDumpCode(true);
        JavaBeanInterfaceGetDate jbid = newNativeReference(JavaBeanInterfaceGetDate.class);
        BytesStore bytes = BytesStore.wrap(ByteBuffer.allocate(64));
        ((Byteable) jbid).bytesStore(bytes, 0L, ((Byteable) jbid).maxSize());
        Date date = new Date();
        jbid.setDate(date);
        assertEquals(date, jbid.getDate());
    }

    @Test
    public void testGenerateInterfaceWithMoreThanOneEnums() {
        //dvg.setDumpCode(true);
        JavaBeanInterfaceMoreThanOneEnums jbid = newNativeReference(JavaBeanInterfaceMoreThanOneEnums.class);
        BytesStore bytes = BytesStore.wrap(ByteBuffer.allocate(64));
        ((Byteable) jbid).bytesStore(bytes, 0L, ((Byteable) jbid).maxSize());
        MyEnum myEnum1 = MyEnum.B;
        jbid.setMyEnum1(myEnum1);
        MyEnum myEnum2 = MyEnum.A;
        jbid.setMyEnum2(myEnum2);
        BuySell buySell = BuySell.BUY;
        jbid.setBuySell(buySell);
        assertEquals(myEnum1, jbid.getMyEnum1());
        assertEquals(myEnum2, jbid.getMyEnum2());
        assertEquals(buySell, jbid.getBuySell());
    }
}
