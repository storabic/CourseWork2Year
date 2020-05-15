import random
from django.db import models
import threading


# Game session
class Game(models.Model):
    #  DONE make unique ensure we can get its number
    key = models.CharField(unique=True, editable=False, max_length=10)
    running = models.BooleanField(default=False)
    ended = models.BooleanField(default=False)
    round = models.IntegerField(default=0)
    is_night = models.BooleanField(default=True)
    time_length = models.IntegerField(default=60)
    chat = models.TextField(default="")
    mafia_chat = models.TextField(default="")
    result = models.TextField(default="")

    def __str__(self):
        return self.key

    # Checks if game is over
    def __is_over(self):
        players = self.player_set.filter(active=True)
        n_mafia = 0
        for player in players:
            if player.role == 2:
                n_mafia += 1
        n_good = len(players) - n_mafia
        if n_good == 0:
            self.result = "Победила мафия"
        elif n_mafia == 0:
            self.result = "Победили мирные жители"
        else:
            return False
        self.save()
        return True

    # Game action like kill, heal and etc.
    def __action(self, choice, by_role):
        player = self.player_set.get(name=choice)
        if by_role == 2 or by_role == 3:
            # mafia or commissar choice
            player.active = False
        elif by_role == 4:
            # doctor choice
            player.active = True
            player.will_be_dead = -1
        elif by_role == 5:
            # lover choice
            player.active = True
            player.will_be_dead = 3
        else:
            raise Exception("Unknown type of action")
        player.save()

    # get game players names
    def get_players(self, name):
        players = []
        for player in self.player_set.all():
            if player.active:
                players.append(player.name)
        return players

    # Cycling and repeating game-round
    def start_round(self):
        if not self.__is_over():
            self.result = ""
            players = self.player_set.filter(active=True)
            if self.is_night:
                self.round += 1
            self.is_night = not self.is_night
            self.save()
            if self.is_night:
                # заканчиваем день
                if self.round == 1:
                    pass
                else:
                    mafia_victims = []
                    for player in players:
                        while len(player.choice) == 0 or players.filter(name=player.choice).count() < 1 or \
                                (player.role != 4 and players.get(name=player.choice).role == player.role):
                            player.choice = random.choice(players).name
                        for victim in mafia_victims:
                            if player.choice == victim[1]:
                                victim[0] += 1
                                break
                        else:
                            mafia_victims.append([1, player.choice])
                    i = 0
                    while i < len(mafia_victims) - 1 and mafia_victims[i] == mafia_victims[i + 1]:
                        i += 1
                    killed_by_mafia = mafia_victims[random.randint(0, i)][1]
                    self.__action(killed_by_mafia, 2)
                    vic = self.player_set.get(name=killed_by_mafia).role
                    if vic == 1:
                        pr = "Мирный житель"
                    elif vic == 2:
                        pr = "Мафия"
                    elif vic == 3:
                        pr = "Комиссар"
                    elif vic == 5:
                        pr = "Любовница"
                    else:
                        pr = "Доктор"
                    self.result = "Был отдан под суд " + killed_by_mafia + " / " + pr + "\n"
            else:
                if self.round == 1:
                    pass
                else:
                    # заканчиваем ночь
                    mafia_victims = []
                    for player in players:
                        if player.will_be_dead != -1:
                            player.will_be_dead -= 1
                            if player.will_be_dead == 0:
                                vic = player.role
                                if vic == 1:
                                    pr = "Мирный житель"
                                elif vic == 2:
                                    pr = "Мафия"
                                elif vic == 3:
                                    pr = "Комиссар"
                                elif vic == 5:
                                    pr = "Любовница"
                                else:
                                    pr = "Доктор"
                                self.result += "Был отравлен любовницей" + player + " / " + pr + "\n"
                        if player.role <= 1:
                            continue
                        while len(player.choice) == 0 or players.filter(name=player.choice).count() < 1 or \
                                (player.role != 4 and players.get(name=player.choice).role == player.role):
                            player.choice = random.choice(players).name
                        if player.role == 2:
                            for victim in mafia_victims:
                                if player.choice == victim[1]:
                                    victim[0] += 1
                                    break
                            else:
                                mafia_victims.append([1, player.choice])
                        else:
                            self.__action(player.choice, player.role)
                            vic = self.player_set.get(name=player.choice).role
                            if vic == 1:
                                pr = "Мирный житель"
                            elif vic == 2:
                                pr = "Мафия"
                            elif vic == 3:
                                pr = "Комиссар"
                            elif vic == 5:
                                pr = "Любовница"
                            else:
                                pr = "Доктор"
                            if player.role == 3:
                                self.result += "Был отдан под арест " + player.choice + " / " + pr + "\n"
                            elif player.role == 4:
                                healed = player.choice
                                self.result += "Был вылечен доктором " + player.choice + " / " + pr + "\n"
                            elif player.role == 5:
                                self.result += "Провёл ночь с любовницей " + player.choice + " / " + pr + "\n"
                    i = 0
                    while i < len(mafia_victims) - 1 and mafia_victims[i] == mafia_victims[i + 1]:
                        i += 1
                    killed_by_mafia = mafia_victims[random.randint(0, i)][1]
                    try:
                        if healed != killed_by_mafia:
                            self.__action(killed_by_mafia, 2)
                    except Exception:
                        self.__action(killed_by_mafia, 2)
                    vic = self.player_set.get(name=killed_by_mafia).role
                    if vic == 1:
                        pr = "Мирный житель"
                    elif vic == 2:
                        pr = "Мафия"
                    elif vic == 3:
                        pr = "Комиссар"
                    elif vic == 5:
                        pr = "Любовница"
                    else:
                        pr = "Доктор"
                    self.result += "Был убит мафией" + killed_by_mafia + " / " + pr + "\n"
            self.save()
            timer = threading.Timer(self.time_length, self.start_round)
            timer.start()
        else:
            self.running = False
            self.ended = True
            self.running = False
            self.save()
            threading.Timer(30, self.delete).start()


# Game player
class Player(models.Model):
    game = models.ForeignKey(Game, on_delete=models.CASCADE)
    name = models.CharField(editable=False, default="Player", max_length=12)
    # 0 - spectator
    # 1 - civilian
    # 2 - mafia
    # 3 - cop
    # 4 - doctor
    # 5 - lover
    role = models.IntegerField(default=0)
    active = models.BooleanField(default=True)
    choice = models.CharField(max_length=12, default="")
    will_be_dead = models.IntegerField(default=-1)
    is_host = models.BooleanField(default=False)

    def __str__(self):
        return self.name
