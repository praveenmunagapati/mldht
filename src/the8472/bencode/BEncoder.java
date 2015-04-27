package the8472.bencode;

import static the8472.bencode.Utils.str2buf;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.stream.Stream;

public class BEncoder {
	
	private ByteBuffer buf;
	
	public static class RawData {
		ByteBuffer rawBuf;
		
		public RawData(ByteBuffer b) {
			rawBuf = b;
		}
	}
	
	public ByteBuffer encode(Map<String, Object> toEnc, int maxSize) {
		buf = ByteBuffer.allocate(maxSize);
		encodeInternal(toEnc);
		buf.flip();
		return buf;
	}
	
	private void encodeInternal(Object o) {
		if(o instanceof Map) {
			encodeMap((Map<String, Object>) o);
			return;
		}
		
		if(o instanceof List) {
			encodeList((List<Object>) o);
			return;
		}
		
		
		if(o instanceof String) {
			encodeLong(((String) o).length(), ':');
			str2buf((String)o, buf);
			return;
		}

		if(o instanceof byte[]) {
			byte[] b = (byte[]) o;
			encodeLong(b.length, ':');
			buf.put(b);
			return;
		}
		
		if(o instanceof ByteBuffer) {
			ByteBuffer clone = ((ByteBuffer) o).slice();
			encodeLong(clone.remaining(), ':');
			buf.put(clone);
			return;
		}
		
		if(o instanceof Integer) {
			buf.put((byte) 'i');
			encodeLong(((Integer) o).longValue(),'e');
			return;
		}
		
		if(o instanceof Long) {
			buf.put((byte) 'i');
			encodeLong(((Long) o).longValue(), 'e');
			return;
		}
		
		if(o instanceof RawData) {
			buf.put(((RawData) o).rawBuf);
			return;
		}
		
		throw new RuntimeException("unknown object to encode " + o);
	}
	
	private void encodeList(List<Object> l) {
		buf.put((byte) 'l');
		l.forEach(e -> encodeInternal(e));
		buf.put((byte) 'e');
	}
	
	private void encodeMap(Map<String, Object> map) {
		buf.put((byte) 'd');
		Stream<Entry<String,Object>> str;
		if(map instanceof SortedMap<?, ?> && ((SortedMap<?, ?>) map).comparator() == null)
			str = map.entrySet().stream();
		else
			str = map.entrySet().stream().sorted(Map.Entry.comparingByKey());
		
		str.forEachOrdered(e -> {
			encodeInternal(e.getKey());
			encodeInternal(e.getValue());
		});
		buf.put((byte) 'e');
	}
	
	private void encodeLong(long val, char terminator) {
		str2buf(Long.toString(val), buf);
		buf.put((byte) terminator);
	}
}
