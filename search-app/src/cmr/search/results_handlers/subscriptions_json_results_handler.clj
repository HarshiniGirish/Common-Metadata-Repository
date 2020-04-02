(ns cmr.search.results-handlers.subscriptions-json-results-handler
  "Handles extracting elasticsearch subscription results and converting them into a JSON search response."
  (:require
   [cheshire.core :as json]
   [clojure.edn :as edn]
   [cmr.common-app.services.search :as qs]
   [cmr.common-app.services.search.elastic-results-to-query-results :as elastic-results]
   [cmr.common-app.services.search.elastic-search-index :as elastic-search-index]
   [cmr.common.util :as util]))

(defmethod elastic-search-index/concept-type+result-format->fields [:subscription :json]
  [concept-type query]
  ["concept-id" "revision-id" "deleted" "provider-id" "native-id" "subscription-name" 
   "subscriber-id" "collection-concept-id"])

(defmethod elastic-results/elastic-result->query-result-item [:subscription :json]
  [context query elastic-result]
  (let [{{[subscription-name] :subscription-name
          [subscriber-id] :subscriber-id
          [collection-concept-id] :collection-concept-id
          [deleted] :deleted
          [provider-id] :provider-id
          [native-id] :native-id
          [concept-id] :concept-id} :fields} elastic-result
        revision-id (elastic-results/get-revision-id-from-elastic-result :service elastic-result)
        result-item (util/remove-nil-keys
                     {:concept_id concept-id
                      :revision_id revision-id
                      :provider_id provider-id
                      :native_id native-id
                      :name subscription-name
                      :subscriber-id subscriber-id
                      :collection-concept-id collection-concept-id})]
    (if deleted
      (assoc result-item :deleted deleted)
      result-item)))

(defmethod qs/search-results->response [:subscription :json]
  [context query results]
  (json/generate-string (select-keys results [:hits :took :items])))
