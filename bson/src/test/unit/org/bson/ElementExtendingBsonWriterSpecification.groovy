/*
 * Copyright 2017 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bson

import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.EncoderContext
import spock.lang.Specification

import static org.bson.BsonHelper.documentWithValuesOfEveryType

class ElementExtendingBsonWriterSpecification extends Specification {

    def 'should write all types'() {
        given:
        def encodedDoc = new BsonDocument();

        when:
        new BsonDocumentCodec().encode(new ElementExtendingBsonWriter(new BsonDocumentWriter(encodedDoc), []),
                documentWithValuesOfEveryType(), EncoderContext.builder().build())

        then:
        encodedDoc == documentWithValuesOfEveryType()
    }

    def 'should extend with extra elements'() {
        given:
        def encodedDoc = new BsonDocument();
        def extraElements = [
                new BsonElement('$db', new BsonString('test')),
                new BsonElement('$readPreference', new BsonDocument('mode', new BsonString('primary')))
        ]
        def expectedDocument = documentWithValuesOfEveryType()
        for (def cur : extraElements) {
            expectedDocument.put(cur.name, cur.value)
        }
        def writer = new ElementExtendingBsonWriter(new BsonDocumentWriter(encodedDoc), extraElements)

        when:
        new BsonDocumentCodec().encode(writer, documentWithValuesOfEveryType(), EncoderContext.builder().build())

        then:
        encodedDoc == expectedDocument
    }

    def 'should extend with extra elements when piping a reader at the top level'() {
        given:
        def encodedDoc = new BsonDocument();
        def extraElements = [
                new BsonElement('$db', new BsonString('test')),
                new BsonElement('$readPreference', new BsonDocument('mode', new BsonString('primary')))
        ]
        def expectedDocument = documentWithValuesOfEveryType()
        for (def cur : extraElements) {
            expectedDocument.put(cur.name, cur.value)
        }
        def writer = new ElementExtendingBsonWriter(new BsonDocumentWriter(encodedDoc), extraElements)

        when:
        writer.pipe(new BsonDocumentReader(documentWithValuesOfEveryType()))

        then:
        encodedDoc == expectedDocument
    }

    def 'should not extend with extra elements when piping a reader at nested level'() {
        given:
        def encodedDoc = new BsonDocument();
        def extraElements = [
                new BsonElement('$db', new BsonString('test')),
                new BsonElement('$readPreference', new BsonDocument('mode', new BsonString('primary')))
        ]
        def expectedDocument = new BsonDocument('pipedDocument', new BsonDocument())
        for (def cur : extraElements) {
            expectedDocument.put(cur.name, cur.value)
        }
        def writer = new ElementExtendingBsonWriter(new BsonDocumentWriter(encodedDoc), extraElements)

        when:
        writer.writeStartDocument()
        writer.writeName('pipedDocument')
        writer.pipe(new BsonDocumentReader(new BsonDocument()))
        writer.writeEndDocument()

        then:
        encodedDoc == expectedDocument
    }
}