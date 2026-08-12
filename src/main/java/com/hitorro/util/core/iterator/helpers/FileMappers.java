/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core.iterator.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.JSONIterator;
import com.hitorro.util.core.iterator.LineReaderIterator;
import com.hitorro.util.core.iterator.StreamTokenizerIterator;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.json.JSONElement;
import com.hitorro.util.json.iterators.HTJSONIterator;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * The iterator-integrated {@link BaseMapper} pipelines that previously lived on
 * {@link FileUtil} as static fields. Moved here so that {@code FileUtil} has no
 * compile-time dependency on the iterator framework. Callers previously doing
 * {@code FileUtil.fsInputStream} now write {@code FileMappers.fsInputStream}.
 */
public final class FileMappers {

    private FileMappers() {}

    public static final BaseMapper<File, InputStream> fsInputStream = new BaseMapper<File, InputStream>() {
        @Override
        public InputStream apply(File bf) {
            try {
                InputStream is = FileUtil.getBufferedFileInputStream(bf);
                if (bf.getName().endsWith("gz")) {
                    is = new GZIPInputStream(is);
                }
                return is;
            } catch (IOException e) {
                com.hitorro.util.core.Log.filesystem.error("Unable to create stream for basefile %s %e", e, e);
                return null;
            }
        }
    };

    public static final BaseMapper<File, OutputStream> fsOutputStream = new BaseMapper<File, OutputStream>() {
        @Override
        public OutputStream apply(File bf) {
            try {
                OutputStream os = FileUtil.getBufferedFileOutputStream(bf);
                if (bf.getName().endsWith("gz")) {
                    os = new GZIPOutputStream(os);
                }
                return os;
            } catch (IOException e) {
                com.hitorro.util.core.Log.filesystem.error("Unable to create stream for basefile %s %e", e, e);
                return null;
            }
        }
    };

    public static final BaseMapper<OutputStream, Writer> os2Utf8Writer = new BaseMapper<OutputStream, Writer>() {
        @Override
        public Writer apply(OutputStream os) {
            return new BufferedWriter(new OutputStreamWriter(os));
        }
    };

    public static final BaseMapper<OutputStream, PrintWriter> os2Utf8PrintWriter = new BaseMapper<OutputStream, PrintWriter>() {
        @Override
        public PrintWriter apply(OutputStream os) {
            return new PrintWriter(new OutputStreamWriter(os));
        }
    };

    public static final BaseMapper<OutputStream, PrintStream> os2Utf8PrintStream = new BaseMapper<OutputStream, PrintStream>() {
        @Override
        public PrintStream apply(OutputStream os) {
            return new PrintStream(os);
        }
    };

    public static final BaseMapper<byte[], InputStream> byteArray2InputStream = new BaseMapper<byte[], InputStream>() {
        @Override
        public InputStream apply(final byte[] e) {
            return new ByteArrayInputStream(e);
        }
    };

    public static final BaseMapper<File, Writer> file2Utf8Writer = fsOutputStream.combine(os2Utf8Writer);

    public static final BaseMapper<InputStream, Reader> is2reader = new BaseMapper<InputStream, Reader>() {
        @Override
        public Reader apply(InputStream is) {
            try {
                return new InputStreamReader(is, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                com.hitorro.util.core.Log.filesystem.error("Unable to open file for reading %s %e", e, e);
                return null;
            }
        }
    };

    public static final BaseMapper<Reader, AbstractIterator<JsonNode>> readerJacksonJsonIter = new BaseMapper<Reader, AbstractIterator<JsonNode>>() {
        @Override
        public AbstractIterator<JsonNode> apply(Reader r) {
            return new JSONIterator(r);
        }
    };

    public static final BaseMapper<Reader, AbstractIterator<JSONElement>> readerJsonIter = new BaseMapper<Reader, AbstractIterator<JSONElement>>() {
        @Override
        public AbstractIterator<JSONElement> apply(Reader r) {
            return new HTJSONIterator(r);
        }
    };

    public static final BaseMapper<Reader, AbstractIterator<JSONElement>> readerJsonIterOfTwitter = new BaseMapper<Reader, AbstractIterator<JSONElement>>() {
        @Override
        public AbstractIterator<JSONElement> apply(Reader r) {
            return new HTJSONIterator(r, "twitter_co");
        }
    };

    public static final BaseMapper<File, Reader> fileReader = fsInputStream.combine(is2reader);
    public static final BaseMapper<File, AbstractIterator<JSONElement>> fsJsonIter = fileReader.combine(readerJsonIter);
    public static final BaseMapper<InputStream, AbstractIterator<JSONElement>> isJsonIter = is2reader.combine(readerJsonIter);

    public static final BaseMapper<Reader, AbstractIterator<String>> reader2stringiter = new BaseMapper<Reader, AbstractIterator<String>>() {
        @Override
        public AbstractIterator<String> apply(Reader reader) {
            return new LineReaderIterator(reader);
        }
    };

    public static final BaseMapper<InputStream, AbstractIterator<JsonNode>> inputstream2JacksonjsonReader =
            is2reader.combine(readerJacksonJsonIter);
    public static final BaseMapper<File, AbstractIterator<JsonNode>> fsStaxJsonIter =
            fsInputStream.combine(inputstream2JacksonjsonReader);

    /**
     * Iterator over tokens produced by a {@link StreamTokenizer}. Formerly
     * {@code FileUtil.getIteratorFromFileStreamTokenizer(File)}.
     */
    public static Iterator<String> tokenizerIterator(File file) throws FileNotFoundException {
        StreamTokenizer tok = FileUtil.getStringTokenizerFromFile(file);
        if (tok == null) return null;
        return new StreamTokenizerIterator(tok);
    }
}
