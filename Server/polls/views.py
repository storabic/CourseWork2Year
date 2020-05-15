import json
import random
from string import ascii_uppercase, digits

import django.core.exceptions as djangoexceptions
import django.db.utils as utils
from django.http import JsonResponse
from numpy.random import permutation

from .models import Game, Player


# Generates random key from range (AAAA - 9999)
def generate_random_key():
    return ''.join(random.choice(ascii_uppercase + digits) for i in range(4))


# Creates new lobby in database
def create_lobby(request):
    if request.method != 'POST':
        ans = {'status': 'failed', 'message': 'POST request expected'}
    else:
        ans = {'status': 'success'}
        data_json = json.loads(request.body)
        name = data_json['name']
        game = Game()
        flag = True
        while flag:
            try:
                game.key = generate_random_key()
                game.save()
                flag = False
            except utils.IntegrityError:
                pass
        user = game.player_set.create(role=0, name=name)
        user.save()
        ans.update({'key': game.key})
        # not needed probably
        # request.session['player'] = name
    return JsonResponse(ans)


# Adds new player sent in request to an existing database
def join_lobby(request):
    if request.method != 'PUT':
        ans = {'status': 'failed', 'message': 'PUT request expected'}
    else:
        ans = {'status': 'success'}
        data_json = json.loads(request.body)
        name = data_json['name']
        if data_json.get('key'):
            wanted_key = data_json['key']
            try:
                game = Game.objects.get(key=wanted_key)
                if game.player_set.filter(name=name).count() > 0:
                    ans = {'status': 'failed', 'message': 'name'}
                    return JsonResponse(ans)
                new_player = game.player_set.create(role=0, name=name)
                new_player.save()
                ans.update({'key': game.key})
            except djangoexceptions.ObjectDoesNotExist:
                ans.update({'status': 'failed', 'message': 'invalid key'})
        else:
            try:
                game = Game.objects.filter(running=False)[0]
                if game.player_set.filter(name=name).count() > 0:
                    ans = {'status': 'failed', 'message': 'name'}
                    return JsonResponse(ans)
                new_player = game.player_set.create(role=0, name=name)
                new_player.save()
                ans.update({'key': game.key})
            except (djangoexceptions.ObjectDoesNotExist, IndexError):
                ans.update({'status': 'failed', 'message': 'no open lobbies'})
    return JsonResponse(ans)


# Delete player sent in request from game in database
def disconnect(request):
    if request.method != 'DELETE':
        ans = {'status': 'failed', 'message': 'DELETE request expected'}
    else:
        ans = {'status': 'success'}
        data_json = json.loads(request.body)
        name = data_json['name']
        wanted_key = data_json['key']
        try:
            game = Game.objects.get(key=wanted_key)
            player = game.player_set.get(name=name)
            player.delete()
            game.save()
        except djangoexceptions.ObjectDoesNotExist:
            ans.update({'status': 'failed', 'message': 'invalid key'})
    return JsonResponse(ans)


# Starts an existing game
def start_game(request):
    if request.method != 'PUT':
        ans = {'status': 'failed', 'message': 'PUT request expected'}
        return JsonResponse(ans)
    ans = {'status': 'success'}
    data_json = json.loads(request.body)
    key = data_json['key']
    n_mafia = data_json['n_mafia']
    cop = data_json['cop']
    doctor = data_json['doctor']
    lover = data_json['lover']
    game = Game.objects.get(key=key)
    players = game.player_set.all()
    n = len(players)
    if (n_mafia > (len(players) // 3 + 1)) or (n_mafia < n // 4) or \
            (n_mafia + cop + doctor + lover > n - 2):
        ans.update({'status': 'failed', 'message': 'invalid roles'})
    else:
        indexes = permutation(n).tolist()
        i = 0
        if cop:
            players[indexes[i]].role = 3
            i += 1
        if doctor:
            players[indexes[i]].role = 4
            i += 1
        if lover:
            players[indexes[i]].role = 5
            i += 1
        while n_mafia > 0:
            players[indexes[i]].role = 2
            i += 1
            n_mafia -= 1
        while i < n:
            players[indexes[i]].role = 1
            i += 1
        for player in players:
            player.save()
        game.running = True
        try:
            game.time_length = data_json['time']
        except KeyError:
            pass
        game.save()
        game.start_round()
    return JsonResponse(ans)


# Make selection of player
def select_player(request):
    if request.method != 'PUT':
        ans = {'status': 'success', 'message': 'PUT request expected'}
        return JsonResponse(ans)
    ans = {'status': 'success'}
    try:
        data_json = json.loads(request.body)
        name = data_json['name']
        choice = data_json['choice']
        player = Player.objects.get(name=name)
        player.choice = choice
        player.save()
    except Exception:
        ans.update({'status': 'failed'})
    return JsonResponse(ans)


# Send message to session chat
def send_message(request):
    ans = {'status': 'success'}
    data_json = json.loads(request.body)
    key = data_json['key']
    name = data_json['name']
    msg = data_json['msg']
    game = Game.objects.get(key=key)
    if not game.is_night or game.round == 0:
        game.chat += name + ": " + msg + "\n"
        game.save()
    if game.is_night and game.player_set.get(name=name).role == 2:
        game.mafia_chat += "|Mafia| " + name + ": " + msg + "\n"
        game.save()
    return JsonResponse(ans)


# Get info(fields) of an existing game
def get_info(request):
    ans = {'status': 'success'}
    if request.method != 'PUT':
        ans.update({'status': 'failed', 'message': 'PUT request expected'})
    else:
        print(request.body)
        data_json = json.loads(request.body)
        key = data_json['key']
        name = data_json['name']
        game = Game.objects.get(key=key)
        player = game.player_set.get(name=name)
        if not player.active or game.ended:
            ans.update({'message': 'end', 'result': game.result})
        else:
            ans.update({'message': 'new', 'day': game.round, 'is_night': game.is_night,
                        'players': game.get_players(name), 'role': player.role, 'chat': game.chat,
                        'result': game.result})
            if player.role == 2:
                friends = []
                for other in game.player_set.all():
                    if other.role == 2:
                        if player != other:
                            friends.append(other.name)
                ans.update({'friends': friends, 'chat': game.chat + game.mafia_chat})
    return JsonResponse(ans)
