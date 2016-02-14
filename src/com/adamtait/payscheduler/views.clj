(ns com.adamtait.payscheduler.views
  (:require [geheimtur.util.auth :refer [get-identity]]
            [ring.util.response :as ring-resp]
            [hiccup.page :as h]
            [hiccup.element :as e]))

(def head
  [:head
   [:title "Geheimtür Demo"]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
   [:link {:href "/css/bootstrap.min.css" :media "screen" :rel "stylesheet" :type "text/css"}]
   [:link {:href "/css/bootstrap-social.css" :media "screen" :rel "stylesheet" :type "text/css"}]
   [:link {:href "//maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css" :media "screen" :rel "stylesheet" :type "text/css"}]
   "<!--[if lt IE 9]>"
   [:script {:src "/js/html5shiv.js"}]
   [:script {:src "/js/respond.min.js"}]
   "<![endif]-->"])

(defn navbar
  [user]
  [:nav {:class "navbar navbar-default" :role "navigation"}
   [:div {:class "navbar-header"}
    [:button {:type "button" :class "navbar-toggle" :data-toggle "collapse" :data-target ".navbar-collapse"}
     [:span {:class "sr-only"} "Toggle navigation"]
     [:span {:class "icon-bar"}]
     [:span {:class "icon-bar"}]
     [:span {:class "icon-bar"}]]
    [:a {:class "navbar-brand" :href "/"} "Geheimtür Demo"]]
   [:div {:class "collapse navbar-collapse"}
    [:ul {:class "nav navbar-nav"}
     [:li
      [:a {:href "/interactive"} "Interactive"]]
     [:li
      [:a {:href "/http-basic"} "HTTP-Basic"]]]
    (when-not (nil? user)
      [:div {:class "navbar-right"}
       [:p {:class "navbar-text"}
        "Signed in as " [:strong (:full-name user)]]
       [:a {:href "/logout" :class "btn btn-primary navbar-btn"}
        "Logout"]])]])

(defn body
  [user & content]
  [:body
   (navbar user)
   [:div {:class "container"}
    [:div {:class "row"}
     content]]
   [:script {:src "//code.jquery.com/jquery.js"}]
   [:script {:src "/js/bootstrap.min.js"}]])

(defn login-form
  [return has-error]
  [:div {:class "col-lg-6 col-lg-offset-3"}
   (when has-error
     [:div {:class "alert alert-danger alert-dismissable"}
      [:button {:type "button" :class "close" :data-dismiss "alert" :aria-hidden "true"} "&times;"]
      "Wrong username and password combination."])
   [:form {:method "POST" :action (if return (str "/login?return=" return) "/login") :accept-charset "UTF-8"}
    [:fieldset
     [:legend "Sign in"]
     [:div {:class "form-group"}
      [:label {:for "username" :class "control-label hidden"} "Username"]
      [:input {:type "text" :class "form-control" :id "username" :name "username" :placeholder "Username" :autocomplete "off"}]]
     [:div {:class "form-group"}
      [:label {:for "password" :class "control-label hidden"} "Password"]
      [:input {:type "password" :class "form-control" :id "password" :name "password" :placeholder "Password"}]]
     [:div {:class "form-group"}
      [:button {:type "submit" :class "btn btn-default btn-block"} "Sign in"]]
     [:legend "or"]
     [:div {:class "row"}
      [:div {:class "col-lg-6"}
       [:a {:class "btn btn-block btn-social btn-lg btn-github" :href (str "/oauth.login?provider=github" (if return (str "&return=" return) ""))}
        [:i {:class "fa fa-github"}] "Sign in with " [:b "Github"]]]
      [:div {:class "col-lg-6"}
       [:a {:class "btn btn-block btn-social btn-lg btn-google" :href (str "/oauth.login?provider=google" (if return (str "&return=" return) ""))}
        [:i {:class "fa fa-google"}] "Sign in with " [:b "Google"]]]]]]])

(defn error-page
  [context]
  (ring-resp/response
   (h/html5 head (body (:user context)
                       [:div {:class "col-lg-8 col-lg-offset-2"}
                        [:h2 (:title context)]
                        [:p (:message context)]]))))

(defn unauthorized
  [request]
  (ring-resp/response
   (h/html5 head (body nil
                       [:div {:class "col-lg-8 col-lg-offset-2"}
                        [:h2 "Unauthorized"]
                        [:p "It looks like there was a problem authenticating you, sir. Please try again."]]))))

(defn home-page
  [request]
  (ring-resp/response
   (h/html5 head (body (get-identity request)
                       [:div {:class "col-lg-8 col-lg-offset-2"}
                        [:p [:a {:href "https://github.com/propan/geheimtur"} "geheimtur"]
                         " - is a collection of interceptors and functions that simplify addition of authentication/authorization to your Pedestal application. "
                         "At this moment, it provides support for interactive (form-based or OAuth2) and http-basic authentications."]
                        [:p "The source code of this application is available on " [:a {:href "https://github.com/propan/geheimtur-demo"} "GitHub"]]
                        [:h3 "Credentials"]
                        [:p "All demos accept the following username/password combinations:"]
                        [:ul
                         [:li [:code "user/password"] " - associated with *user* role"]
                         [:li [:code "admin/password"] " - assosiated with *admin* role"]]]))))

(defn login-page
  [{:keys [params] :as request}]
  (let [has-error (contains? params :error)]
    (ring-resp/response (h/html5 head (body (get-identity request) (login-form (:return params) has-error))))))
