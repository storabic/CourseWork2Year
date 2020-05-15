from django.contrib import admin
from django.urls import path
from polls import views

urlpatterns = [
    path('admin/', admin.site.urls),
    path(r'api/create_lobby', views.create_lobby, name='create_lobby'),
    path(r'api/join_lobby', views.join_lobby, name='join_lobby'),
    path(r'api/disconnect', views.disconnect, name='disconnect'),
    path(r'api/start_game', views.start_game, name='start_game'),
    path(r'api/select_player', views.select_player, name='select_player'),
    path(r'api/get_info', views.get_info, name='get_info'),
    path(r'api/send_msg', views.send_message, name='send_message'),
]
