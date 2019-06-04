package burp;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;

import burp.Methods;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import javax.swing.JOptionPane;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

@RunWith(PowerMockRunner.class)
public class MethodsTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @PrepareForTest({JOptionPane.class, Methods.class})
  @Test
  public void testPrompt_and_validate_input() {
    PowerMockito.mockStatic(JOptionPane.class);
    PowerMockito.when(JOptionPane.showInputDialog(anyString(),
            anyString())).thenReturn(null, "", "fooBar");

    Assert.assertNull(
            Methods.prompt_and_validate_input("foo", "Bar"));
    Assert.assertEquals("fooBar",
            Methods.prompt_and_validate_input("foo", "Bar"));
  }

  @Test
  public void testDecimalToHex() {
    Assert.assertEquals("0", Methods.decimalToHex(0));
    Assert.assertEquals("7C", Methods.decimalToHex(124));
  }

  @Test
  public void testDecodingException() throws UnsupportedEncodingException {
    thrown.expect(NullPointerException.class);
    Methods.decoding(null);
  }

  @Test
  public void testDecoding() throws UnsupportedEncodingException {
    Assert.assertArrayEquals("Bar".getBytes(),
            Methods.decoding("foo\r\nBar\r\n;".getBytes()));
  }

  @Test
  public void testEncodingException() throws UnsupportedEncodingException {
    thrown.expect(NullPointerException.class);
    Methods.encoding(null, 0, false);
  }

  @Test
  public void testEncodingFalse() throws UnsupportedEncodingException {
    Assert.assertArrayEquals("3\r\nfoo\r\n3\r\nBar\r\n0\r\n\r\n".getBytes(),
            Methods.encoding("fooBar".getBytes(), 3, false));
  }

  @PrepareForTest({Random.class, Methods.class})
  @Test
  public void testEncodingTrue() throws Exception {
    final Random random = PowerMockito.mock(Random.class);
    PowerMockito.when(random.nextInt(anyInt())).thenReturn(5);
    PowerMockito.whenNew(Random.class).withNoArguments().thenReturn(random);

    Assert.assertArrayEquals(
          "3;FFFFFFFFFF\r\nfoo\r\n3;FFFFFFFFFF\r\nBar\r\n0\r\n\r\n".getBytes(),
          Methods.encoding("fooBar".getBytes(), 3, true));
  }

  @Test
  public void testDo_modify_request() {
    Assert.assertArrayEquals("fooBaro".getBytes(),
            Methods.do_modify_request("foo".getBytes(),
                    new int[]{3, 2}, "Bar".getBytes()));
  }

  @Test
  public void testDo_modify_requestNull() {
    Assert.assertArrayEquals("foo".getBytes(),
            Methods.do_modify_request("foo".getBytes(),
                    new int[]{3, 2}, null));
  }

  @PrepareForTest({Random.class, Methods.class})
  @Test
  public void testGetRandomString() throws Exception {
    final Random random = PowerMockito.mock(Random.class);
    PowerMockito.when(random.nextInt(anyInt())).thenReturn(5, 14);
    PowerMockito.whenNew(Random.class).withNoArguments().thenReturn(random);

    Assert.assertEquals("FOO", Methods.getRandomString(3));
  }

  @Test
  public void testGetStrList() {
    final ArrayList<String> arrayList = new ArrayList<String>();
    arrayList.add("foo");
    arrayList.add("Bar");
    arrayList.add("1");

    Assert.assertEquals(arrayList,
            Methods.getStrList("fooBar1", 3));
  }

  @Test
  public void testGetStrListWithSize() {
    final ArrayList<String> arrayList = new ArrayList<String>();
    arrayList.add("foo");
    arrayList.add("Bar");

    Assert.assertEquals(arrayList,
            Methods.getStrList("fooBar123", 3, 2));
  }

  @Test
  public void testSubstring() {
    Assert.assertNull(Methods.substring("12345", 6, 7));

    Assert.assertEquals("345",
            Methods.substring("12345", 2, 5));
    Assert.assertEquals("45",
            Methods.substring("12345", 3, 6));
  }
}
